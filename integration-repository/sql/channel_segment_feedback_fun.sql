CREATE OR REPLACE FUNCTION channel_segment_feedback_fun_11052015(last_updated timestamp without time zone)
RETURNS SETOF channel_segment_feedback_type_11052015 AS
$BODY$
DECLARE
    row1    channel_segment_feedback_type_11052015%ROWTYPE;
BEGIN
    FOR row1 IN
	SELECT  account_id AS advertiser_id ,
            adgroup_id AS ad_group_id ,
                case   total_server_impression=0
                when true THEN 0.0
                else (total_revenue/total_server_impression)*1000
            END AS ecpm ,
                case   total_request=0
                when true THEN 0.0
                else total_fills/total_request
            END AS fill_ratio ,
            today_impressions,
            GREATEST   (wap_channel_adgroup.modified_on,
                    latest_earnings_dcp_feedback.modified_on,
                    daily_feedback.modified_on) AS modified_on
    FROM 	wap_channel_adgroup ,
            (
                SELECT  account_id ,
                        segment_id ,
                        sum(fills) AS total_fills ,
                        sum(server_impression/(DATE(NOW())-DATE(served_on))) AS total_server_impression,
                        sum(request) AS total_request ,
                        sum(revenue/(DATE(NOW())-DATE(served_on))) AS total_revenue ,
                        max(modified_on) AS modified_on
                FROM earnings_dcp_feedback
                WHERE served_on > (NOW()-15)
                GROUP BY account_id ,segment_id
            ) AS latest_earnings_dcp_feedback ,
            (
                SELECT  external_site_key,
                        sum(server_impression) AS today_impressions,
                        max(modified_on) AS modified_on
                FROM realtime_dcp_feedback
                WHERE DATE(NOW()) = DATE(served_on)
                GROUP BY external_site_key
                UNION ALL
                SELECT  external_site_key,
                        today_impressions,
                        modified_on
                FROM(
                        SELECT  external_site_key,
                                0 AS today_impressions,
                                DATE(NOW()) + time '00:00:00' AS modified_on,
                                max(DATE(served_on)) AS max_served_on
                        FROM    realtime_dcp_feedback
                        GROUP BY external_site_key
                    )
                WHERE   DATE(max_served_on) < DATE(NOW())
            ) AS daily_feedback
    WHERE 	wap_channel_adgroup.external_site_key = latest_earnings_dcp_feedback.segment_id
    AND     daily_feedback.external_site_key = latest_earnings_dcp_feedback.segment_id
    AND     (wap_channel_adgroup.modified_on >= last_updated
             or latest_earnings_dcp_feedback.modified_on >= last_updated
             or daily_feedback.modified_on >= last_updated
            )

LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
