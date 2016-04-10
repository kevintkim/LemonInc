package com.lemoninc.nimbusrun.Networking.Server;

/*********************************
 * FILENAME : TapTapServer.java
 * DESCRIPTION :
 * PUBLIC FUNCTIONS :
 * void    update(float delta)
 * void    shutdown()
 * NOTES :
 * LAST UPDATED: 8/4/2016 09:00
 ********************************/

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.lemoninc.nimbusrun.Networking.Network;
import com.lemoninc.nimbusrun.Sprites.GameMap;
import com.lemoninc.nimbusrun.Sprites.Player;

import java.io.IOException;

public class TapTapServer {

    //Kryonet Server object
    Server server;

    private GameMap map;

    /**
     * Constructor starts a server on port 8080 and adds a listener to the server
     */
    public TapTapServer() {
        server = new Server() {
            protected Connection newConnection() {
                //Provide our own implementation of connection so that we can refer
                // to each connection by the client's name in the Listener
                return new TapTapConnection();
            }
        };

        map = new GameMap(this);

        Network.registerClasses(server);

/**
 * server listens for messages from the clients.
 *
 * Message could be about map state:
 * obstacle coordinates
 * player joined/left
 * etc
 *
 */
        server.addListener(new Listener() {
            public void received(Connection c, Object message) {
                TapTapConnection connection = (TapTapConnection) c;

                if (message instanceof Network.Login) {
                    logInfo("Login received");
                    Network.Login msg = ((Network.Login) message);

                    if (connection.name != null) {return;}

                    String name = msg.name;
                    if (name == null) {return;}
                    name = name.trim();
                    if (name.length() == 0) {return;}//if name contains no letters
                    //name this connection as the clientname
                    connection.name = name;

                    //tell the new client about map state (obstacle coordinates ...)
                    //add this new player to gamemap
                    Network.PlayerJoinLeave newPlayer = new Network.PlayerJoinLeave(connection.getID(), connection.name, true);
                    //tell old clients about new client
                    server.sendToAllExceptTCP(connection.getID(), newPlayer);
                    logInfo("Adding the new Client to Server's map");
//                    map.addPlayer(newPlayer);

                    //tell new client about old clients
//                    for (Connection con : server.getConnections()) { //upon connection, every client's name is stored in Player
//                        TapTapConnection conn = (TapTapConnection) con;
//                        if (conn.getID() != connection.getID() && conn.name != null) { // Not self, Have logged in
//                            Player herePlayer = map.getPlayerById(conn.getID());
//                            Network.PlayerJoinLeave hereMsg = new Network.PlayerJoinLeave(conn.getID(), herePlayer.getName(), true);
//                            logInfo("Telling "+connection.name+" about old client "+herePlayer.getName());
//                            connection.sendTCP(hereMsg); // basic info
////                            connection.sendTCP(herePlayer.getMovementState()); // info about current movement
//                        }
//                    }
                }
            }

            public void disconnected(Connection c) {
                TapTapConnection connection = (TapTapConnection) c;
                //announce to everyone that someone got disconnected
            }
        });

        try {
            server.bind(Network.PORT, Network.PORTUDP);

        } catch (IOException e) {
            e.printStackTrace();
        }


        server.start();
    }

    public void update(float delta) {
        map.update(delta); //TODO:make sure server's map.update doesn't contain rendering
    }

    public void shutdown() {
        server.close();
        server.stop();
    }

    static class TapTapConnection extends Connection {
        public String name;
    }

    private void logInfo(String string) {
        Log.info("[TapTapServer]: " + string);
    }
}

