/**
 *
 */
package com.inmobi.adserve.channels.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.Policy;
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
    private final WritePolicy writePolicy;
    private final Policy readPolicy;
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

        writePolicy = new WritePolicy();
        writePolicy.expiration = -1;

        readPolicy = new Policy();
    }

    /**
     * 
     * @param key
     * @param binName
     * @param binValue
     * @throws AerospikeException
     */
    public void writeBin(final String key, final String binName, final String binValue) throws AerospikeException {
        aerospikeClient.put(writePolicy, new Key(namespace, setName, key), new Bin(binName, binValue));
    }

    /**
     * 
     * @param binName
     * @param key
     * @throws AerospikeException
     */
    public void deleteBin(final String binName, final String key) throws AerospikeException {
        aerospikeClient.put(writePolicy, new Key(namespace, setName, key), Bin.asNull(binName));
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
        final boolean[] bool = aerospikeClient.exists(writePolicy, keys);
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
     * @throws AerospikeException
     */
    public static void main(final String[] args) throws IOException, AerospikeException {
        System.out.println("Enter your choice : \n1 Insert Data \n2 Read Data");
        final BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        final AeroSpikeDataIngestion aeroSpike = new AeroSpikeDataIngestion(HOST, PORT, NAMESPACE, SET);

        final String queryType = bufferRead.readLine();
        switch (queryType) {
            case "1":
                System.out.println("Enter Full File path to read:");
                final String filename = bufferRead.readLine();
                final List<String> lines = FileUtils.readLines(new File(filename));

                aeroSpike.insertData(lines);
                break;
            case "2":
                System.out.println("Enter keys (Enter empty key to stop): ");

                List<String> keyList = new ArrayList<>();
                String key;
                do {
                    key = bufferRead.readLine();
                    if (StringUtils.isNotBlank(key)) {
                        keyList.add(key);
                    }

                } while (StringUtils.isNotBlank(key));

                aeroSpike.getFromAerospike(keyList, BIN_NAME);
                break;
            default:
                System.err.println("Wrong input !!!");
                break;
        }
    }

    private void getFromAerospike(final List<String> keys, final String binName) {
        final Key key;
        try {
            for (final String _key : keys) {
                final Record record = aerospikeClient.get(readPolicy, new Key(NAMESPACE, SET, _key), binName);
                System.out.println(_key + " -> " + record.bins.get(binName));
            }
        } catch (AerospikeException e) {
            e.printStackTrace();
        }
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
