/**
 *
 */
package com.inmobi.adserve.channels.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;

/**
 * @author ritwik.kumar
 *
 */
public class AeroSpikeDataIngestion {
    private static final String HOST = "as.ads.hkg1.inmobi.com";
    private static final int PORT = 3000;
    private static final String NAMESPACE = "device_imei";
    private static final String SET = "imei";
    private static final String BIN_NAME = "imei";

    private static final int NO_OF_THREADS = 300;

    private final AerospikeClient aerospikeClient;
    private final WritePolicy policy;
    private final String namespace;
    private final String setName;
    private static int totalCount = 0;

    /**
     * 
     * @param host
     * @param port
     * @param namespace
     * @param set
     * @throws AerospikeException
     */
    public AeroSpikeDataIngestion(final String host, final int port, final String namespace, final String set)
            throws AerospikeException {
        this.namespace = namespace;
        setName = set;
        final ClientPolicy clientPolicy = new ClientPolicy();
        clientPolicy.maxThreads = NO_OF_THREADS;
        aerospikeClient = new AerospikeClient(clientPolicy, host, port);

        policy = new WritePolicy();
        policy.expiration = -1;
    }

    /**
     * 
     * @param key
     * @param binName
     * @param binValue
     * @throws AerospikeException
     */
    public void writeBin(final String key, final String binName, final String binValue) throws AerospikeException {
        aerospikeClient.put(policy, new Key(namespace, setName, key), new Bin(binName, binValue));
    }

    /**
     * 
     * @param binName
     * @param key
     * @throws AerospikeException
     */
    public void deleteBin(final String binName, final String key) throws AerospikeException {
        aerospikeClient.put(policy, new Key(namespace, setName, key), Bin.asNull(binName));
    }

    /**
     * 
     * @param keySet
     * @throws AerospikeException
     */
    public void exists(final Set<String> keySet) throws AerospikeException {
        final Key[] keys = new Key[keySet.size()];
        int count = 0;
        for (final String keyStr : keySet) {
            keys[count++] = new Key(namespace, setName, keyStr);
        }
        final boolean[] bool = aerospikeClient.exists(policy, keys);
        for (int i = 0; i < bool.length; i++) {
            if (!bool[i]) {
                System.out.println(keys[i].userKey);
            }
        }
    }

    /**
     * 
     * @param lines
     */
    public void insertData(final List<String> lines) {
        final int size = lines.size() / NO_OF_THREADS;
        final List<List<String>> smallerLists = Lists.partition(lines, size);
        int count = 0;
        for (final List<String> smallerList : smallerLists) {
            final Thread job = new Job(smallerList, this, String.valueOf(++count));
            job.start();
        }
    }

    /**
     * 
     * @param line
     * @return
     */
    private static String[] getKeyAndVal(final String line) {
        final String spilitStr[] = line.split(",");
        final String key = spilitStr[0];
        final String val = spilitStr[1].split("\\|")[0];
        return new String[] {key, val};
    }

    /**
     * @param args
     * @throws IOException
     * @throws InitializationException
     */
    public static void main(final String[] args) throws IOException, AerospikeException {
        final BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter Full File path to read :");
        final String filename = bufferRead.readLine();
        final List<String> lines = FileUtils.readLines(new File(filename));

        final AeroSpikeDataIngestion aeroSpike = new AeroSpikeDataIngestion(HOST, PORT, NAMESPACE, SET);
        aeroSpike.insertData(lines);
    }

    @AllArgsConstructor
    private static class Job extends Thread {
        private final List<String> lines;
        private final AeroSpikeDataIngestion aeroSpike;
        private final String name;

        @Override
        public void run() {
            String key = null, val = null;
            int success = 0, failure = 0;
            for (final String line : lines) {
                try {
                    final String spilitStr[] = getKeyAndVal(line);
                    key = spilitStr[0];
                    val = spilitStr[1];
                    aeroSpike.writeBin(key, BIN_NAME, val);
                    success++;
                    totalCount++;
                } catch (final Exception e) {
                    System.err.println(key + "->" + val);
                    System.err.println(e.getMessage());
                    failure++;
                }
                System.out.println(String.format("name ->%s success ->%s failure ->%s totalCount ->%s", name, success,
                        failure, totalCount));
            }
        }
    }

}
