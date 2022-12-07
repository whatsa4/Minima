package org.minima.tests.cli;

import org.minima.Minima;
import org.minima.objects.base.MiniNumber;
import org.minima.system.Main;
import org.minima.system.network.webhooks.NotifyManager;
import org.minima.system.params.TestParams;
import org.minima.utils.MinimaLogger;
import org.minima.utils.json.JSONArray;
import org.minima.utils.json.JSONObject;
import org.minima.utils.json.parser.JSONParser;
import org.minima.utils.messages.Message;
import org.minima.utils.messages.MessageListener;

import java.util.ArrayList;


public class MinimaTestNode {

    static String command = "";
    public Minima minima;

    public MinimaTestNode() {

        //New Instance of Minima
        minima = new Minima();

        //Add a listener
        Main.setMinimaListener(new MessageListener() {

            @Override
            public void processMessage(Message zMessage) {
                if (zMessage.getMessageType().equals(MinimaLogger.MINIMA_LOG)) {

                } else if (zMessage.getMessageType().equals(NotifyManager.NOTIFY_POST)) {
                    //Get the JSON..
                    JSONObject notify = (JSONObject) zMessage.getObject("notify");

                    //What is the Event..
                    String event = (String) notify.get("event");
                    JSONObject data = (JSONObject) notify.get("data");

                    if (event.equals("NEWBLOCK")) {
                        //Get the TxPoW
                        JSONObject mTxPowJSON = (JSONObject) data.get("txpow");

                    } else if (event.equals("NEWBALANCE")) {
                        //Balance change..
                    }
                }
            }
        });


        //Now start her up..
        ArrayList<String> vars = new ArrayList<>();

        vars.add("-daemon");
        vars.add("-nop2p");
        vars.add("-test");
        vars.add("-genesis");

        //Set a data dir..
        //vars.add("-data");
        //vars.add("");

        //No shut down hook for CTRL C
        //vars.add("-noshutdownhook");

        //Auto connect
        //vars.add("-connect");
        //vars.add("127.0.0.1:9001");
        TestParams.MINIMA_BLOCK_SPEED = new MiniNumber("0.2"); //faster blockspeed for testing

        minima.mainStarter(vars.toArray(new String[0]));

//Catch error..
        try {

            //Get the status..
            JSONObject json = new JSONObject();
            boolean statusOK = false;
            while (!statusOK) {
                MinimaLogger.log("Waiting for Status .. " + json.toString());

                Thread.sleep(2000);

                //Run Status..
                String status = minima.runMinimaCMD("status");

                //Make a JSON
                json = (JSONObject) new JSONParser().parse(status);

                //Are we ok
                statusOK = (boolean) json.get("status");
            }

            //Wait for Network to definitely start up
            Thread.sleep(1000);

        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void setCommand(String _command) {
        command = _command;
    }

    public String runCommand() {
        String output = minima.runMinimaCMD(command, false);
        return output.toString();
    }

    public String runCommand(String command) throws Exception {
        this.setCommand(command);
        String output = this.runCommand();
        return output;
    }

    public boolean waitForMinimaBlockConfirmation() throws Exception {

        long balance = 0;
        int attempts = 0;
        while (balance == 0 && attempts <= 250) {
            Thread.sleep(1000);

            this.setCommand("balance");
            String balanceOutput = this.runCommand();

            var jsonObject = (JSONObject) new JSONParser().parse(balanceOutput.toString());
            JSONArray innerJson = (JSONArray) jsonObject.get("response");
            JSONObject innerJsonObj = (JSONObject) innerJson.get(0);

            balance = Long.valueOf(innerJsonObj.get("confirmed").toString()).longValue();
            attempts++;
        }
        return attempts != 250;
    }

    public int getCurrentBlock() throws Exception {
        String statusOutput = this.runCommand("status");
        var jsonObject = (JSONObject) new JSONParser().parse(statusOutput.toString());
        JSONObject responseJson = (JSONObject) jsonObject.get("response");
        JSONObject chainJson = (JSONObject) responseJson.get("chain");

        String block = chainJson.get("block").toString();
        return Integer.parseInt(block);
    }

    public String getNewAddress() throws Exception {
        //get my new address
        String getaddressResponse = this.runCommand("newaddress");

        var stringResponseObj = (JSONObject) new JSONParser().parse(getaddressResponse);
        JSONObject stringResponseResponseObj = (JSONObject) stringResponseObj.get("response");
        String address = stringResponseResponseObj.get("address").toString();

        return address;
    }

    public String getAddress() throws Exception {
        //get a default address
        String getaddressResponse = this.runCommand("getaddress");

        var stringResponseObj = (JSONObject) new JSONParser().parse(getaddressResponse);
        JSONObject stringResponseResponseObj = (JSONObject) stringResponseObj.get("response");
        String address = stringResponseResponseObj.get("address").toString();

        return address;
    }

    public String getPublicKey() throws Exception {
        String getaddressOutput = minima.runMinimaCMD("getaddress");

        JSONObject json = (JSONObject) new JSONParser().parse(getaddressOutput);

        var responseAttr = json.get("response");

        JSONObject responseObj = (JSONObject) new JSONParser().parse(responseAttr.toString());

        return responseObj.get("publickey").toString();
    }

    public void killMinima() {
        minima.runMinimaCMD("quit", false);
    }
}