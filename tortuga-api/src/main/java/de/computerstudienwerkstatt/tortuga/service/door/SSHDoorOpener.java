package de.computerstudienwerkstatt.tortuga.service.door;

import de.computerstudienwerkstatt.tortuga.util.NetworkUtil;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.computerstudienwerkstatt.tortuga.model.cabinet.Cabinet;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Mischa Holz
 */
public class SSHDoorOpener implements DoorOpener {

    private static Logger logger = LoggerFactory.getLogger(DoorOpener.class);

    private String host;

    private String user;

    private String password;

    private String fingerPrint;

    public SSHDoorOpener(String host, String user, String password, String fingerPrint) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.fingerPrint = fingerPrint;
    }

    private void executeCommandOnHost(String cmdStr) {
        Thread thread = new Thread(() -> {
            try(SSHClient ssh = new SSHClient()) {
                ssh.addHostKeyVerifier(fingerPrint);

                ssh.connect(host);

                ssh.authPassword(user, password);

                try(Session session = ssh.startSession()) {
                    Session.Command cmd = session.exec(cmdStr);
                    cmd.join(5, TimeUnit.SECONDS);
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();
    }

    @Override
    public void openCabinetDoor(Cabinet cabinet) {
        if(!NetworkUtil.isLocalNetworkRequest()) {
            logger.warn("NOT OPENING DOOR {} BECAUSE NOT LOCAL NETWORK", cabinet);
            return;
        }

        logger.warn("OPENING CABINET DOOR {}", cabinet);

        String cmdStr = "~/cabinet";
        if(cabinet == Cabinet.CABINET_6) {
            cmdStr += "6";
        } else if(cabinet == Cabinet.CABINET_7) {
            cmdStr += "7";
        } else {
            throw new IllegalArgumentException("You forgot to add the new cabinet here");
        }
        cmdStr += "Open.sh";

        executeCommandOnHost(cmdStr);
    }

    @Override
    public void openRoomDoor() {
        if(!NetworkUtil.isLocalNetworkRequest()) {
            logger.warn("NOT OPENING ROOM DOOR BECAUSE NOT LOCAL NETWORK");
            return;
        }

        openRoomDoorWithoutCheckingNetwork();
    }

    @Override
    public void openRoomDoorWithoutCheckingNetwork() {
        logger.warn("OPENING ROOM DOOR");

        executeCommandOnHost("~/doorOpen.sh 3");
    }
}
