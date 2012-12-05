CREATE OR REPLACE FUNCTION channel_segment_feedback_fun(last_updated timestamp without time zone)
RETURNS SETOF channel_segment_feedback_type AS
$BODY$
DECLARE
    row1    channel_segment_feedback_type%ROWTYPE;
BEGIN
    FOR row1 IN
	select account_id as advertiser_id ,
		adgroup_id as ad_group_id ,
		case   total_server_impression=0 
			when true then 0.0 
			else (total_revenue/total_server_impression)*1000 
		end as ecpm ,
		case   total_request=0 
			when true then 0.0 
			else total_fills/total_request 
		end as fill_ratio 
	from wap_channel_adgroup , 
	(	select account_id , 
			segment_id , 
			sum(fills) as total_fills , 
			sum(server_impression/(date(now())-date(served_on))) as total_server_impression, 
			sum(request) as total_request , 
			sum(revenue/(date(now())-date(served_on))) as total_revenue , 
			max(modified_on) as modified_on 
		from earnings_dcp_feedback 
		where served_on > (now()-15) 
		group by account_id ,segment_id
	) 
	as latest_earnings_dcp_feedback 
	where  advertiser_id = account_id and
		external_site_key = segment_id and
		latest_earnings_dcp_feedback.modified_on >= last_updated


 
LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
