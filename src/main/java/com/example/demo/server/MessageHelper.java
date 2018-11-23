package com.example.demo.server;

import com.example.demo.bean.CcsInMessage;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper for the transformation of JSON messages to attribute maps and vice
 * versa in the XMPP Server
 */

public class MessageHelper {

	/**
	 * Creates a JSON from a FCM outgoing message attributes
	 */


	public static String createJsonAck(String to, String messageId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("message_type", "ack");
		map.put("to", to);
		map.put("message_id", messageId);
		return createJsonMessage(map);
	}

	public static String createJsonMessage(Map<String, Object> jsonMap) {
		return JSONValue.toJSONString(jsonMap);
	}

	public static CcsInMessage createCcsInMessage(Map<String, Object> jsonMap) {
		String from = jsonMap.get("from").toString();
		// Package name of the application that sent this message
		String category = jsonMap.get("category").toString();
		// Unique id of this message
		String messageId = jsonMap.get("message_id").toString();
		Map<String, String> dataPayload = (Map<String, String>) jsonMap.get("data");
		CcsInMessage msg = new CcsInMessage(from, category, messageId, dataPayload);
		return msg;
	}

}
