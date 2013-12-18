CREATE OR REPLACE FUNCTION channel_feedback_fun_modified_on_fix(last_updated timestamp without time zone)
RETURNS SETOF channel_feedback_type AS
$BODY$
DECLARE
    row1    channel_feedback_type%ROWTYPE;
BEGIN
    FOR row1 IN
	select * from (
	select  coalesce(final_realtime_dcp_feedback.advertiser_id,
            dcp_advertiser_burn.advertiser_id) as id,
            total_inflow,
            burn as total_burn,
            balance,
            0 as average_latency,
            1 as total_impressions,
            today_impressions,
            today_requests,
            revenue,
            now() as modified_on
	from dcp_advertiser_burn full outer join
	(
			select  coalesce(aggregated_realtime_dcp_feedback.advertiser_id,
			                revenue_feedback.account_id) as advertiser_id,
                    revenue,
                    today_impressions,
                    today_requests
			from
			(
			        select account_id,
					max(revenue) as revenue 
				    from (  select account_id,
                            sum(revenue)as revenue
                            from (  select distinct segment_id,
                                    account_id,
                                    revenue,
                                    served_on
                                    from earnings_dcp_feedback
                                    )
                            where served_on > (now()-15)
                            group by account_id,served_on
					        )
                    group by account_id
            ) as revenue_feedback
            full outer join
            (
                    select * from ( select advertiser_id,
                                    sum(server_impression) as today_impressions,
                                    sum(request) as today_requests
                                    from realtime_dcp_feedback
                                    where date(served_on) = date(now())
                                    group by advertiser_id
                                    )
                    union all
                    select * from ( select advertiser_id,
                                    today_impressions,
                                    today_requests
                                    from (	select advertiser_id,
                                            0 as today_impressions,
                                            0 as today_requests,
                                            date(now()) + time '00:00:00' as modified_on,
                                            max(date(served_on)) as max_served_on
                                            from realtime_dcp_feedback
                                            group by advertiser_id
                                        )
                                    where date(max_served_on) < date(now())
                            )
            ) as aggregated_realtime_dcp_feedback
		    on (aggregated_realtime_dcp_feedback.advertiser_id = revenue_feedback.account_id)
	) as final_realtime_dcp_feedback
	on  (final_realtime_dcp_feedback.advertiser_id=dcp_advertiser_burn.advertiser_id)) 
LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
