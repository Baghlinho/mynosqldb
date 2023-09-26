package com.capstone.node;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Stream;

public class StartUp {

    public static void main(String[] args) throws Exception {
        int numNodes = Integer.parseInt(args[0]);

        // build the image from the docker file
        exec("docker build -t nosqlnode ./nosqlnode");
        Thread.sleep(1000 * 10);

        exec("docker network create --subnet=10.1.4.0/28 cluster");
        exec("docker",
                "run",
                "-d",
                "-p",
                "8000:8080",
                "--name", "bootstrap",
                "--network", "cluster",
                "--ip", "10.1.4.10",
                "--env", "NODE_ID=0",
                "--env","NUM_NODES="+ numNodes,
                "--env",
                "BOOTSTRAP=yes",
                "nosqlnode");
        Thread.sleep(1000 * 5);

       for(int i = 1; i <= numNodes; i++) {
            exec("docker",
                    "run",
                    "-d",
                    "-p", 8000 + i + ":8080",
                    "--name", "worker_" + i,
                    "--network", "cluster",
                    "--ip", "10.1.4.1" + i,
                    "--env", "NODE_ID=" + i,
                    "nosqlnode");
        }
    }

    static void exec(String... args) throws IOException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        List<String> commands;
        if (isWindows)
            commands = Stream.of("cmd.exe", "/c").collect(Collectors.toList());
        else
            commands = Stream.of("sh", "-c").collect(Collectors.toList());
        commands.addAll(Arrays.asList(args));
        System.out.println(commands);
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.start();
    }

}
