CREATE OR REPLACE FUNCTION channel_segment_feedback_fun_modified_on_fix(last_updated timestamp without time zone)
RETURNS SETOF channel_segment_feedback_type_17042013 AS
$BODY$
DECLARE
    row1    channel_segment_feedback_type_17042013%ROWTYPE;
BEGIN
    FOR row1 IN
	select  account_id as advertiser_id ,
            adgroup_id as ad_group_id ,
                case   total_server_impression=0
                when true then 0.0
                else (total_revenue/total_server_impression)*1000
            end as ecpm ,
                case   total_request=0
                when true then 0.0
                else total_fills/total_request
            end as fill_ratio ,
            today_impressions,
            least   (wap_channel_adgroup.modified_on,
                    latest_earnings_dcp_feedback.modified_on,
                    daily_feedback.modified_on) as modified_on
    from 	wap_channel_adgroup ,
            (
                select  account_id ,
                        segment_id ,
                        sum(fills) as total_fills ,
                        sum(server_impression/(date(now())-date(served_on))) as total_server_impression,
                        sum(request) as total_request ,
                        sum(revenue/(date(now())-date(served_on))) as total_revenue ,
                        max(modified_on) as modified_on
                from earnings_dcp_feedback
                where served_on > (now()-15)
                group by account_id ,segment_id
            ) as latest_earnings_dcp_feedback ,
            (
                select  external_site_key,
                        sum(server_impression) as today_impressions,
                        max(modified_on) as modified_on
                from realtime_dcp_feedback
                where date(now()) = date(served_on)
                group by external_site_key
                union all
                select  external_site_key,
                        today_impressions,
                        modified_on
                from(
                        select  external_site_key,
                                0 as today_impressions,
                                date(now()) + time '00:00:00' as modified_on,
                                max(date(served_on)) as max_served_on
                        from    realtime_dcp_feedback
                        group by external_site_key
                    )
                where   date(max_served_on) < date(now())
            ) as daily_feedback
    where 	wap_channel_adgroup.external_site_key = latest_earnings_dcp_feedback.segment_id
    and     daily_feedback.external_site_key = latest_earnings_dcp_feedback.segment_id
    and     (wap_channel_adgroup.modified_on >= last_updated
             or latest_earnings_dcp_feedback.modified_on >= last_updated
             or daily_feedback.modified_on >= last_updated
            )

LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
