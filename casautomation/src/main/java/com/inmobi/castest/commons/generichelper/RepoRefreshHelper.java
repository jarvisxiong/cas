package com.inmobi.castest.commons.generichelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.inmobi.castest.utils.common.CasE2EDBDetails;
import net.sf.json.JSONObject;

import org.apache.http.client.ClientProtocolException;

import com.inmobi.castest.casconfenums.def.CasConf.Repo;
import com.inmobi.castest.casconfenums.impl.RepoConf;
import com.inmobi.castest.utils.common.CasDBDetails;
import com.inmobi.castest.utils.common.CasServerDetails;

public class RepoRefreshHelper {
    public static void RefreshRepo() throws Exception {
        String repoRefreshUrl = new String();
        final JSONObject jsonParams = new JSONObject();
        jsonParams.put("DBHost", CasDBDetails.getDbHost());
        jsonParams.put("DBPort", CasDBDetails.getDbPort());
        if (CasDBDetails.isE2e()) {
            jsonParams.put("DBSnapshot", CasE2EDBDetails.getDbName());
        } else {
            jsonParams.put("DBSnapshot", CasDBDetails.getDbName());
        }
        jsonParams.put("DBUser", CasDBDetails.getDbUserName());
        jsonParams.put("DBPassword", CasDBDetails.getDbPassword());

        for (final Repo repoName : Repo.values()) {

            jsonParams.put("repoName", RepoConf.setRepoNameForRefresh(repoName));

            System.out.println("json string - " + jsonParams.toString());

            repoRefreshUrl = CasServerDetails.getCasServerEndPoint() + "repoRefresh?args=" + jsonParams.toString();

            System.out.println(repoRefreshUrl);

            send(repoRefreshUrl);
        }
    }

    public static void RefreshRepo(final String repoName) throws InterruptedException, ClientProtocolException,
            IOException {
        String repoRefreshUrl = new String();
        final JSONObject jsonParams = new JSONObject();
        jsonParams.put("repoName", repoName);
        jsonParams.put("DBHost", CasDBDetails.getDbHost());
        jsonParams.put("DBPort", CasDBDetails.getDbPort());
        jsonParams.put("DBSnapshot", CasDBDetails.getDbName());
        jsonParams.put("DBUser", CasDBDetails.getDbUserName());
        jsonParams.put("DBPassword", CasDBDetails.getDbPassword());

        System.out.println("json string - " + jsonParams.toString());

        repoRefreshUrl = CasServerDetails.getCasServerEndPoint() + "repoRefresh?args=" + jsonParams.toString();

        System.out.println(repoRefreshUrl);
        System.out.println("Refreshing Repo : " + jsonParams.get("repoName"));

        send(repoRefreshUrl);
        Thread.sleep(2000);
    }

    private static void send(final String repoRefreshUrl) {
        // specific refresh for IX , rtbd etc
        try {

            final URL url = new URL(repoRefreshUrl);
            String repoRefreshOutput = new String();
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            Thread.sleep(1000);
            System.out.println("Response Message :******" + conn.getResponseMessage());
            final InputStreamReader x = new InputStreamReader(conn.getInputStream());
            final BufferedReader br = new BufferedReader(x);

            System.out.println("Repo Refresh Status");
            System.out.println("===================");
            while ((repoRefreshOutput = br.readLine()) != null) {
                System.out.println(repoRefreshOutput);
            }
            conn.disconnect();
            br.close();
            x.close();
        } catch (final Exception e) {

            e.printStackTrace();
        }
    }
}
