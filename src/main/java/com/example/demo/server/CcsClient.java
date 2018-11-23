package com.example.demo.server;


import com.example.demo.bean.CcsInMessage;
import com.example.demo.util.Util;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSocketFactory;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//@Component
public class CcsClient implements PacketListener {

    public static final Logger logger = Logger.getLogger(CcsClient.class.getName());

    private static CcsClient sInstance = null;
    private XMPPConnection connection;
    private ConnectionConfiguration config;
    private String mApiKey = null;
    private String mProjectId = null;
    private boolean mDebuggable = false;
    private String fcmServerUsername = null;

    public static CcsClient getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("You have to prepare the client first");
        }
        return sInstance;
    }

    public static CcsClient prepareClient(String projectId, String apiKey, boolean debuggable) {
        synchronized (CcsClient.class) {
            if (sInstance == null) {
                sInstance = new CcsClient(projectId, apiKey, debuggable);
            }
        }
        return sInstance;
    }

    private CcsClient(String projectId, String apiKey, boolean debuggable) {
        this();
        mApiKey = apiKey;
        mProjectId = projectId;
        mDebuggable = debuggable;
        fcmServerUsername = mProjectId + "@" + Util.FCM_SERVER_CONNECTION;
    }

    private CcsClient() {
        // Add GcmPacketExtension
        ProviderManager.getInstance().addExtensionProvider(Util.FCM_ELEMENT_NAME, Util.FCM_NAMESPACE,
                (PacketExtensionProvider) parser -> {
                    String json = parser.nextText();
                    GcmPacketExtension packet = new GcmPacketExtension(json);
                    return packet;
                });
    }

    public void connect() throws XMPPException {
        config = new ConnectionConfiguration(Util.FCM_SERVER, Util.FCM_PORT);
        config.setSecurityMode(SecurityMode.enabled);
        config.setReconnectionAllowed(true);
        config.setRosterLoadedAtLogin(false);
        config.setSendPresence(false);
        config.setSocketFactory(SSLSocketFactory.getDefault());
        // Launch a window with info about packets sent and received
        config.setDebuggerEnabled(mDebuggable);

        connection = new XMPPConnection(config);
        connection.connect();

        connection.addConnectionListener(new ConnectionListener() {

            @Override
            public void reconnectionSuccessful() {
            }

            @Override
            public void reconnectionFailed(Exception e) {
            }

            @Override
            public void reconnectingIn(int seconds) {
            }

            @Override
            public void connectionClosedOnError(Exception e) {
            }

            @Override
            public void connectionClosed() {
            }
        });

        // Handle incoming packets (the class implements the PacketListener)
        connection.addPacketListener(this, new PacketTypeFilter(Message.class));

        // Log all outgoing packets
        connection.addPacketInterceptor(packet -> {
            //logger.log(Level.INFO, "Sent: {0}", packet.toXML());
        }, new PacketTypeFilter(Message.class));

        connection.login(fcmServerUsername, mApiKey);
        logger.log(Level.INFO, "Logged in: " + fcmServerUsername);
    }

    public void reconnect() {

    }

    /**
     * Handles incoming messages
     */
    @Override
    public void processPacket(Packet packet) {
        logger.log(Level.INFO, "Received: " + packet.toXML());
        Message incomingMessage = (Message) packet;
        GcmPacketExtension gcmPacket = (GcmPacketExtension) incomingMessage.getExtension(Util.FCM_NAMESPACE);
        String json = gcmPacket.getJson();
        try {
            Map<String, Object> jsonMap = (Map<String, Object>) JSONValue.parseWithException(json);
            Object messageType = jsonMap.get("message_type");

            if (messageType == null) {
                CcsInMessage inMessage = MessageHelper.createCcsInMessage(jsonMap);
                handleUpstreamMessage(inMessage); // normal upstream message
                return;
            }

        } catch (ParseException e) {
            logger.log(Level.INFO, "Error parsing JSON: " + json, e.getMessage());
        }

    }

    /**
     * Handles an upstream message from a device client through FCM
     */
    private void handleUpstreamMessage(CcsInMessage inMessage) {
        // Send ACK to FCM
        String ack = MessageHelper.createJsonAck(inMessage.getFrom(), inMessage.getMessageId());
        send(ack);
    }

    /**
     * Sends a downstream message to FCM
     */
    public void send(String jsonRequest) {
        Packet request = new GcmPacketExtension(jsonRequest).toPacket();
        connection.sendPacket(request);
    }

}
