package io.firebus.adapters;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.logging.Logger;
import io.firebus.utils.StreamReceiver;
import io.firebus.utils.StreamSender;

/**
 * Dynamic manager client for SFTP servers, the SFTP counterpart of the AWS
 * S3DynamicManager. Stateless: every request carries the host, credentials
 * and paths, so one registered instance serves any number of remote servers.
 * Files are streamed through the firebus file service, never buffered whole.
 *
 * Supported actions:
 * - put:    write a file service file (by fileuid) to the server; written to
 *           a temp name first, renamed on completion, so the other side never
 *           picks up a half written file
 * - get:    read a remote file into the file service, returns the new fileuid
 * - list:   list the file names in a remote directory
 * - delete: remove a remote file
 * - rename: rename or move a remote file
 *
 * Authentication:
 * - password, or a private key when the secret starts with -----BEGIN
 *   (with an optional passphrase)
 * - when a hostkey (base64 public key) is provided the server is verified
 *   against it; otherwise host key checking is off and a warning is logged
 */
public class SftpDynamicManager implements ServiceProvider {
	protected DataMap config;
	protected Firebus firebus;

	public SftpDynamicManager(DataMap c, Firebus fb) {
		config = c;
		firebus = fb;
	}

	/**
	 * Request fields:
	 * - always:  action, host (or host:port), username
	 * - auth:    password or privatekey, optional passphrase and hostkey
	 * - put:     path, fileuid, fileservice
	 * - get:     path, fileservice, optional newfilename
	 * - list:    path
	 * - delete:  path
	 * - rename:  from, to
	 */
	public Payload service(Payload payload) throws FunctionErrorException {
		Session session = null;
		ChannelSftp channel = null;
		try {
			DataMap request = payload.getDataMap();
			String action = request.getString("action");
			String host = request.getString("host");
			int port = request.containsKey("port") ? request.getNumber("port").intValue() : 22;
			if(host != null && host.indexOf(":") > -1) {
				port = Integer.parseInt(host.substring(host.indexOf(":") + 1));
				host = host.substring(0, host.indexOf(":"));
			}
			String username = request.getString("username");
			String password = request.getString("password");
			String privateKey = request.getString("privatekey");
			String passphrase = request.getString("passphrase");
			String hostKey = request.getString("hostkey");
			if(privateKey == null && password != null && password.startsWith("-----BEGIN")) {
				privateKey = password;
				password = null;
			}
			JSch jsch = new JSch();
			if(privateKey != null)
				jsch.addIdentity(username, privateKey.getBytes(), null, passphrase != null ? passphrase.getBytes() : null);
			if(hostKey != null && !hostKey.equals("")) {
				jsch.getHostKeyRepository().add(new HostKey(host, Base64.getDecoder().decode(hostKey)), null);
			} else {
				Logger.warning("fb.adapters.sftpman.nohostkey", "No host key configured for " + host + ", skipping host verification");
			}
			session = jsch.getSession(username, host, port);
			if(password != null)
				session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", hostKey != null && !hostKey.equals("") ? "yes" : "no");
			session.connect(15000);
			channel = (ChannelSftp)session.openChannel("sftp");
			channel.connect(10000);
			DataMap respMap = new DataMap();
			final Throwable[] streamError = new Throwable[1];

			if(action.equals("list")) {
				String path = request.getString("path");
				Vector<?> entries = channel.ls(path == null || path.equals("") ? "." : path);
				DataList respList = new DataList();
				for(Object o: entries) {
					ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry)o;
					if(!entry.getFilename().equals(".") && !entry.getFilename().equals(".."))
						respList.add(entry.getFilename());
				}
				respMap.put("list", respList);

			} else if(action.equals("put")) {
				String path = request.getString("path");
				String fileUid = request.getString("fileuid");
				String fileService = request.getString("fileservice");
				DataMap streamReq = new DataMap("action", "get", "fileuid", fileUid);

				Payload streamReqPayload = new Payload(streamReq);
				streamReqPayload.metadata.put("session", payload.metadata.get("session"));
				streamReqPayload.metadata.put("token", payload.metadata.get("token"));
				StreamEndpoint sep = firebus.requestStream(fileService, streamReqPayload, 10000);

				if(path == null || path.equals("")) {
					Payload acceptPayload = sep.getAcceptPayload();
					if(acceptPayload != null)
						path = acceptPayload.getDataMap().getString("filename");
				}
				String tempPath = path + ".part";
				OutputStream os = channel.put(tempPath, ChannelSftp.OVERWRITE);

				StreamReceiver receiver = new StreamReceiver(os, sep, new StreamReceiver.CompletionListener() {
					public void error(Throwable error) {
						Logger.severe("fb.adapters.sftpman.put", error);
						streamError[0] = error;
					}

					public byte[] completed() throws Exception {
						Logger.info("fb.adapters.sftpman.put");
						return new byte[0];
					}
				});
				receiver.sync();
				os.close();
				sep.close();
				if(streamError[0] != null) {
					try { channel.rm(tempPath); } catch(Exception e2) { }
					throw new Exception("Error receiving the file stream to put", streamError[0]);
				}
				channel.rename(tempPath, path);
				respMap.put("result", "ok");
				respMap.put("path", path);

			} else if(action.equals("get")) {
				String path = request.getString("path");
				String filename = request.getString("newfilename");
				if(filename == null)
					filename = path.lastIndexOf("/") > -1 ? path.substring(path.lastIndexOf("/") + 1) : path;
				String fileService = request.getString("fileservice");
				DataMap streamReq = new DataMap("action", "put", "filename", filename);

				Payload streamReqPayload = new Payload(streamReq);
				streamReqPayload.metadata.put("mime", "application/json");
				streamReqPayload.metadata.put("session", payload.metadata.get("session"));
				streamReqPayload.metadata.put("token", payload.metadata.get("token"));
				StreamEndpoint sep = firebus.requestStream(fileService, streamReqPayload, 10000);

				InputStream is = channel.get(path);

				StreamSender sender = new StreamSender(is, sep, new StreamSender.CompletionListener() {
					public void error(Throwable error) {
						Logger.severe("fb.adapters.sftpman.get", error);
						streamError[0] = error;
					}

					public void completed(byte[] bytes) {
						try {
							DataMap filemeta = new DataMap(new ByteArrayInputStream(bytes));
							respMap.put("fileuid", filemeta.getString("fileuid"));
							Logger.info("fb.adapters.sftpman.get");
						} catch(Exception e) {
							Logger.severe("fb.adapters.sftpman.get", e);
						}
					}
				});
				sender.sync();
				is.close();
				sep.close();
				if(streamError[0] != null)
					throw new Exception("Error sending the file stream", streamError[0]);

			} else if(action.equals("delete")) {
				String path = request.getString("path");
				channel.rm(path);
				respMap.put("result", "ok");

			} else if(action.equals("rename")) {
				String from = request.getString("from");
				String to = request.getString("to");
				channel.rename(from, to);
				respMap.put("result", "ok");
			}
			return new Payload(respMap);
		} catch(Exception e) {
			throw new FunctionErrorException("Error in SftpManager", e, 400);
		} finally {
			if(channel != null)
				channel.disconnect();
			if(session != null)
				session.disconnect();
		}
	}


	public ServiceInformation getServiceInformation() {
		return null;
	}

}
