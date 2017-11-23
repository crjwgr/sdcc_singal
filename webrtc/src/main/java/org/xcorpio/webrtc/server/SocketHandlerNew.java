package org.xcorpio.webrtc.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

public class SocketHandlerNew {

	public static StreamsManager streamsManager = new StreamsManager();
	public static Map clientMap = new HashMap();
	public static Map reflectedtMap = new HashMap();

	public static Map reallytMap = new HashMap();

	public static void main(String[] args) {

		Configuration config = new Configuration();
		//服务端口
		config.setPort(8888);
		try {
			config.setKeyStore(new FileInputStream(new File(
					"C:/Users/wulin/Desktop/tomcatcer/my.jks")));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		config.setKeyStorePassword("214221338700304");
		config.setKeyStoreFormat("jks");
		config.setSSLProtocol("TLSv1.2");

		final SocketIOServer server = new SocketIOServer(config);
		server.start();// 启动服务
		server.addConnectListener(new ConnectListener() {
			@Override
			public void onConnect(SocketIOClient client) {
				System.out.println("-- " + client.getSessionId().toString()
						+ " joined --");
				client.sendEvent("success", client.getSessionId());
				
			}
		});

		server.addEventListener("readyToStream", Map.class,
				new DataListener<Map>() {

					@Override
					public void onData(SocketIOClient client, Map data,
							AckRequest ackSender) throws Exception {
						System.out.println(data.toString());
						streamsManager.addStream(client.getSessionId()
								.toString(), data.get("name").toString(), "1");
					}

				});

		server.addEventListener("update", Map.class, new DataListener<Map>() {
			@Override
			public void onData(SocketIOClient client, Map data,
					AckRequest ackSender) throws Exception {
				System.out.println(data.toString());
				streamsManager.updateStream(client.getSessionId().toString(),
						data.get("name").toString(), "1");
			}

		});

		// 发送信息

		server.addEventListener("sendMsg", Map.class, new DataListener<Map>() {

			@Override
			public void onData(SocketIOClient client, Map data,
					AckRequest ackSender) throws Exception {

				String sessionId = data.get("friendId") + "";
				String uid = clientMap.get(sessionId).toString();
				System.out.println("发送方:" + client.getSessionId());
				SocketIOClient otherClient = (SocketIOClient) server
						.getClient(UUID.fromString(uid));
				if (otherClient == null) {
					return;
				}
				try {
					otherClient.sendEvent("receiveMsg", data);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});

		server.addEventListener("resetId", Map.class, new DataListener<Map>() {
			@SuppressWarnings("unchecked")
			@Override
			public void onData(SocketIOClient client, Map data,
					AckRequest ackSender) throws Exception {

				clientMap.put(data.get("myId").toString(), client
						.getSessionId().toString());
				client.sendEvent("id", data.get("myId").toString());
				reflectedtMap.put(data.get("myId").toString(), data.get("myId")
						.toString());
				reallytMap.put(client.getSessionId().toString(),
						data.get("myId").toString());
				System.out.println("myId:" + data.get("myId").toString() + " "
						+ client.getSessionId());
				Map createMap = new HashMap();
				createMap.put("status", "1");
				createMap.put("id", data.get("myId").toString());
				doPost("https://www.sdcti.site:8889/ifsy/user/updateAccount",
						createMap, "utf-8");
			}
		});

		server.addEventListener("message", Map.class, new DataListener<Map>() {

			@Override
			public void onData(SocketIOClient client, Map data,
					AckRequest ackSender) throws Exception {
				Map map = new HashMap();
				System.out.println("消息类型:" + data.get("type"));
				String sessionId = data.get("to") + "";
			    if(data.get("type").equals("answer")){
				
				 System.out.println("chichun:"+clientMap.size());
				 @SuppressWarnings("unused")
				String uid = clientMap.get(sessionId).toString();
               
			    }
			    String uid = clientMap.get(sessionId).toString();
				System.out.println("发送方:" + client.getSessionId());

				SocketIOClient otherClient = (SocketIOClient) server
						.getClient(UUID.fromString(uid));
				if (otherClient == null) {
					return;
				}
				map.put("from", data.get("myId"));
				map.put("type", data.get("type"));
				map.put("payload", data.get("payload"));
				System.out.println("接收方:" + otherClient.getSessionId());
				try {
					otherClient.sendEvent("message", map);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		server.addEventListener("startclient", Map.class,
				new DataListener<Map>() {

					@Override
					public void onData(SocketIOClient client, Map data,
							AckRequest ackSender) throws Exception {
						String sessionId = data.get("to") + "";
						String uid = clientMap.get(sessionId).toString();
						SocketIOClient otherClient = server.getClient(UUID
								.fromString(uid));
						// data.put("name", accounts.get(0).getName());
						otherClient.sendEvent("receiveCall", data);
					}

				});

		server.addEventListener("opentab", Map.class, new DataListener<Map>() {

			@Override
			public void onData(SocketIOClient client, Map data,
					AckRequest ackSender) throws Exception {
				Map map = new HashMap();
				String sessionId = data.get("id") + "";
				String uid = clientMap.get(sessionId).toString();
				SocketIOClient mclient = server.getClient(UUID.fromString(uid));
				String id = (String) reallytMap.get(client.getSessionId()
						.toString());
				map.put("id", id);
				mclient.sendEvent("opentab", map);
				/*
				 * } else{ SocketIOClient otherClient =
				 * server.getClient(UUID.fromString
				 * (clientMap.get(data.get("to").toString()).toString()));
				 * data.put("from",
				 * reflectedtMap.get(data.get("to").toString()));
				 * otherClient.sendEvent("receiveCall", data); }
				 */
			}

		});

		server.addEventListener("ejectcall", Map.class,
				new DataListener<Map>() {

					@Override
					public void onData(SocketIOClient client, Map data,
							AckRequest ackSender) throws Exception {
						SocketIOClient otherClient = server.getClient(UUID
								.fromString(clientMap.get(
										data.get("callerId").toString())
										.toString()));
						if (otherClient == null) {
							return;
						}
						otherClient.sendEvent("ejectcall", "");

					}

				});

		server.addEventListener("removecall", Map.class,
				new DataListener<Map>() {

					@Override
					public void onData(SocketIOClient client, Map data,
							AckRequest ackSender) throws Exception {
						SocketIOClient otherClient = server.getClient(UUID
								.fromString(clientMap.get(
										data.get("callerId").toString())
										.toString()));
						if (otherClient == null) {
							return;
						}
						otherClient.sendEvent("removecall", "");

					}

				});

		server.addEventListener("acceptcall", Map.class,
				new DataListener<Map>() {
					@Override
					public void onData(SocketIOClient client, Map data,
							AckRequest ackSender) throws Exception {
						SocketIOClient otherClient = server.getClient(UUID
								.fromString(clientMap.get(
										data.get("callerId").toString())
										.toString()));
						System.out.println(data.get("callerId").toString());
						if (otherClient == null) {
							return;
						}
						otherClient.sendEvent("acceptcall", data);

					}

				});

		server.addEventListener("chat", Map.class, new DataListener<Map>() {

			@Override
			public void onData(SocketIOClient client, Map data,
					AckRequest ackSender) throws Exception {
				Map map = new HashMap();
				SocketIOClient otherClient = server.getClient(UUID
						.fromString(clientMap.get(data.get("to").toString())
								.toString()));
				if (otherClient == null) {
					return;
				}
				otherClient.sendEvent("chat", data);

				/*
				 * map.put("username","zidy123"); map.put("avatar",
				 * "http://tp1.sinaimg.cn/1571889140/180/40030060651/1");
				 * map.put("id",data.get("user_id").toString()); m
				 * map.put("content","2121212"); map.put("type","friend");
				 * otherClient.sendEvent("chats",map);
				 */

			}

		});

		server.addEventListener("leave", Map.class, new DataListener<Map>() {

			@Override
			public void onData(SocketIOClient client, Map data,
					AckRequest ackSender) throws Exception {

				leave(client.getSessionId().toString());
			}

		});

		server.addDisconnectListener(new DisconnectListener() {

			@Override
			public void onDisconnect(SocketIOClient client) {
				// TODO Auto-generated method stub
				Map createMap = new HashMap();
				createMap.put("status", "0");
				createMap.put("id",
						reallytMap.get(client.getSessionId().toString()));
				doPost("https://www.sdcti.site:8889/ifsy/user/updateAccount",
						createMap, "utf-8");
				leave(client.getSessionId().toString());
			}
		});
		try {
			Thread.sleep(Integer.MAX_VALUE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		server.stop();
	}

	public static void leave(String clientId) {
		System.out.println("----" + clientId + "left----");
		streamsManager.removeStream(clientId);
		reallytMap.remove(clientId);
		Collection<String> col = clientMap.values();
		col.remove(clientId);
	}

	public static String doPost(String url, Map<String, String> map,
			String charset) {
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try {
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			// 设置参数
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			Iterator iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> elem = (Entry<String, String>) iterator
						.next();
				list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
			}
			if (list.size() > 0) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,
						charset);
				httpPost.setEntity(entity);
			}
			HttpResponse response = httpClient.execute(httpPost);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
}
