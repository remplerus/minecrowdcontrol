package com.racerxdl.minecrowdcontrol;

import com.google.gson.JsonSyntaxException;
import com.racerxdl.minecrowdcontrol.CrowdControl.EffectResult;
import com.racerxdl.minecrowdcontrol.CrowdControl.Request;
import com.racerxdl.minecrowdcontrol.CrowdControl.RequestType;
import com.racerxdl.minecrowdcontrol.CrowdControl.Response;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.racerxdl.minecrowdcontrol.Tools.makeTranslation;

public class ControlServer {
    private static final Logger Log = LogManager.getLogger();

    private final MinecraftServer server;
    private Player player;
    private final AtomicBoolean running;
    private Minecraft client;
    private PlayerStates states;
    private final ScheduledExecutorService executorService;

    public ControlServer(MinecraftServer server) {
        this.server = server;
        this.running = new AtomicBoolean(false);
        this.states = new PlayerStates();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        Commands.SetEnablePlayerMessages(true);
    }

    public void Start() {
        if (running.compareAndSet(false, true)) {
            Log.info("Starting server");
            Thread t = new Thread(this::serverLoop);
            t.start();
        } else {
            Log.error("Already running");
        }
    }

    public void Stop() {
        running.set(false);
    }

    public PlayerStates GetStates() {
        return this.states.Clone();
    }

    private void SetStates(PlayerStates states) {
        this.states = states.Clone();
    }

    public void DrunkModeLoop() {
        PlayerStates states = GetStates();
        if (states.getDrunkMode()) {
            if (client != null) {
                if (EphemeralStates.DrunkModeFOVIncreasing) {
                    client.options.fov += Tools.FOV_STEP;
                    if (client.options.fov >= Tools.MAX_FOV) {
                        EphemeralStates.DrunkModeFOVIncreasing = false;
                    }
                } else {
                    client.options.fov -= Tools.FOV_STEP;
                    if (client.options.fov <= Tools.MIN_FOV) {
                        EphemeralStates.DrunkModeFOVIncreasing = true;
                    }
                }
            }
            executorService.schedule(this::DrunkModeLoop, Tools.DRUNK_REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
        } else {
            Log.info("Stopping drunk mode loop");
            client.options.fov = GetStates().getOriginalFOV();
        }
    }

    public void SetClient(Minecraft client) {
        Log.info("Setting client!");
        this.client = client;
        SetStates(GetStates().setOriginalFOV(client.options.fov));
    }

    public void SetPlayer(Player player) {
        Log.info("Setting player to " + player.toString());
        this.player = player;
        if (GetStates().getGottaGoFast()) {
            executorService.schedule(() -> {
                Log.info("Re-enabling Gotta Go Fast");
                SetStates(GetStates().setGottaGoFast(false));
                RunCommand("GOTTA_GO_FAST", GetStates().getGottaGoFastViewer(), RequestType.Start);
            }, 500, TimeUnit.MILLISECONDS);
        }
    }

    private void serverLoop() {
        int retries = 0;
        Log.info("Server loop started");
        while (running.get()) {
            if (retries <= 3) {
                try {
                    Commands.SendSystemMessage(server, ChatFormatting.AQUA + makeTranslation("crowdcontrol.connect"));
                    Log.info("Trying to connect to Crowd Control");
                    Socket s = new Socket("localhost", 58430);
                    Log.info("Connected to crowd control!");
                    Commands.SendSystemMessage(server, ChatFormatting.GREEN + makeTranslation("crowdcontrol.connected"));
                    clientLoop(s);
                    Commands.SendSystemMessage(server, ChatFormatting.RED + makeTranslation("crowdcontrol.disconnected"));
                    Log.info("Disconnected from crowd control");
                } catch (Exception e) {
                    Log.error("Socket error: " + e.getMessage());
                    retries++;
                }
            } else {
                Commands.SendSystemMessage(server, ChatFormatting.DARK_RED + makeTranslation("crowdcontrol.unable_2_connect"));
                break;
            }
        }
        Log.info("Server loop ended");
    }

    public void clientLoop(Socket client) {
        try {
            InputStreamReader inFromClient = new InputStreamReader(client.getInputStream());
            DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());
            while (running.get()) {
                String data = Tools.ReadUntilNull(inFromClient);
                Log.debug("Received: " + data);
                String result = ParseJSON(data);
                Log.debug("Sending: " + result);
                byte[] tmp = result.getBytes();
                byte[] outData = new byte[tmp.length + 1];

                System.arraycopy(tmp, 0, outData, 0, tmp.length);

                outData[outData.length - 1] = 0x00;

                outToClient.write(outData);
            }
        } catch (Exception e) {
            Log.error("Error handling client data: " + e.getMessage());
        }
    }

    public CommandResult RunCommand(String command, String viewer, RequestType type) {
        CommandResult res = new CommandResult(GetStates()).SetEffectResult(EffectResult.Unavailable);

        if (Commands.CommandList.get(command.toUpperCase()) != null) {
            res = Commands.CommandList.get(command.toUpperCase()).Run(GetStates(), player, client, server, viewer, type);
        } else {
            Log.error("Command {} not found", command);
        }

        SetStates(res.GetPlayerStates());

        return res;
    }

    public void ScheduleCommand(String cmd, int seconds) {
        Log.info("Scheduling cmd {} STOP after {} seconds", cmd, seconds);
        executorService.schedule(() -> {
            Log.info("Running STOP for {}", cmd);
            CommandResult res = RunCommand(cmd, "server", RequestType.Stop);
            Log.info("Run result: {}", res.GetEffectResult());
        }, seconds, TimeUnit.SECONDS);
    }

    public String ParseJSON(String data) {
        Response res = new Response();
        try {
            Request req = Request.FromJSON(data);
            res.id = req.id;
            CommandResult cmdRes = RunCommand(req.code, req.viewer, req.type);
            res.status = cmdRes.GetEffectResult();
            res.message = "Effect " + req.code + ": " + res.status;

            int sendStopAfter = Timings.GetStopTiming(req.code);

            if (res.status == EffectResult.Success && sendStopAfter > 0) {
                ScheduleCommand(req.code, sendStopAfter);
            }

            if (req.type == RequestType.Start && res.status == EffectResult.Success && req.code.equalsIgnoreCase("DRUNK_MODE")) {
                Log.info("Starting drunk mode loop");
                SetStates(GetStates().setOriginalFOV(client.options.fov));
                DrunkModeLoop();
            }

            return res.ToJSON();
        } catch (JsonSyntaxException e) {
            res.message = e.getMessage();
            return res.ToJSON();
        }
    }
}
