#!/bin/sh

exec 2>&1


[ -n "$HOSTNAME" ] && { hst_name=$HOSTNAME; echo "true $hst_name"; } || { hst_name=`hostname`; echo "false $hst_name"; }

node_name=${hst_name%%.*}
collectd_conf="/opt/inmobi/ops/collectd/etc/collectd.conf"
enode_name=`grep -w Hostname $collectd_conf | awk -F\" '{print $2}'`

if [ "$node_name" = "$enode_name" ]; then
 exec /opt/inmobi/ops/collectd/sbin/collectd -C /opt/inmobi/ops/collectd/etc/collectd.conf -f
else
echo "Hostname configured wrongly in conf. Reintall the package or manually correct the hostname in conf"
fi
