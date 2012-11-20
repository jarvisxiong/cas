CREATE OR REPLACE FUNCTION channel_feedback_fun(last_updated timestamp without time zone)
RETURNS SETOF channel_feedback_type AS
$BODY$
DECLARE
    row1    channel_feedback_type%ROWTYPE;
BEGIN
    FOR row1 IN
	select * 
	from 	(select coalesce(final_realtime_dcp_feedback.advertiser_id,dcp_advertiser_burn.advertiser_id) as id,
			total_inflow, 
			burn as total_burn, 
			balance,
			0 as average_latency,
			-1 as total_impressions,
			today_impressions,
			revenue,
			coalesce(final_realtime_dcp_feedback.modified_on,dcp_advertiser_burn.modified_on) as modified_on
		from dcp_advertiser_burn 
		full outer join  
			(select coalesce(aggregated_realtime_dcp_feedback.advertiser_id,revenue_feedback.account_id) as advertiser_id,
				revenue,
				today_impressions,
				modified_on
			from    (select account_id , 
					max(revenue) as revenue 
				from earnings_dcp_feedback 
				where served_on > (now()-15) group by account_id)
			as revenue_feedback 	 
			full outer join
				(select advertiser_id,
					sum(server_impression) as today_impressions, 
					max(modified_on) as modified_on 
				from realtime_dcp_feedback 
				where date(served_on) = date(now())
				group by advertiser_id) 
			as aggregated_realtime_dcp_feedback 
			on (aggregated_realtime_dcp_feedback.advertiser_id = revenue_feedback.account_id)
			) 
		as final_realtime_dcp_feedback 
		on 
		(final_realtime_dcp_feedback.advertiser_id=dcp_advertiser_burn.advertiser_id))
	as channel_feedback
	where channel_feedback.modified_on > last_updated


 
LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
