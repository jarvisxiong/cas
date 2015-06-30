package com.inmobi.castest.utils.bidders;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Timer;

import com.inmobi.castest.utils.bidders.stats.InspectorStats;
import com.inmobi.castest.utils.bidders.stats.SleepTimeCalculatorTask;
import com.inmobi.castest.utils.common.DummyBidderDetails;

public class Main {

    public static void hostDummyBidder(final String[] args) throws Exception {

        String port = null;
        int waitMultiplier = 0;
        int adRatio = 0;
        double budget = 0;
        String seatId = null;
        boolean underStress = false;
        if (args.length == 6) {
            port = args[0];
            waitMultiplier = Integer.parseInt(args[1]);
            adRatio = Integer.parseInt(args[2]);
            if ("infinity".equalsIgnoreCase(args[3])) {
                budget = Double.POSITIVE_INFINITY;
            } else {
                budget = Double.parseDouble(args[3]);
            }
            seatId = args[4];
            underStress = Boolean.parseBoolean(args[5]);
        } else if (args.length < 7) {
            System.out.println("You did not give port, waitTimeInMillis, AD ratio, budget and seatId");
            System.out.println("Enter port for the DummyBidder");
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            port = bufferedReader.readLine();
            System.out.println("Enter waitTimeInMillis for timeOut");
            waitMultiplier = Integer.parseInt(bufferedReader.readLine());
            System.out.println("Out of 100 how many times this should return an AD:");
            adRatio = Integer.parseInt(bufferedReader.readLine());
            System.out.println("Enter the budget:");
            final String budgetStr = bufferedReader.readLine();
            if ("infinity".equalsIgnoreCase(budgetStr)) {
                budget = Double.POSITIVE_INFINITY;
            } else {
                budget = Double.parseDouble(budgetStr);
            }
            System.out.println("Enter the seatId");
            seatId = bufferedReader.readLine();
            System.out.println("Enter underStress");
            underStress = Boolean.parseBoolean(bufferedReader.readLine());
        }

        initStats(underStress);
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        final ServerBootstrap bootstrap = new ServerBootstrap();

        final PooledByteBufAllocator allocator = new PooledByteBufAllocator(true);
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(Integer.parseInt(port)))
                .childHandler(new PipelineFactory(waitMultiplier, adRatio, budget, seatId, underStress))
                // disable nagle's algorithm
                .childOption(ChannelOption.TCP_NODELAY, true)
                // allow binding channel on same ip, port
                .childOption(ChannelOption.SO_REUSEADDR, true).childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, allocator).bind().sync();

        System.out.println("Server is running at port :" + port + ", waitTimeInMillis : " + waitMultiplier
                + ", adRatio: " + adRatio + ", budget is: " + budget + " and seatId is" + seatId);
    }

    private static void initStats(final boolean underStress) {

        InspectorStats.init("mon02.ads.lhr1.inmobi.com", 2003, 1);
        if (underStress) {
            final Timer timer = new Timer();
            timer.scheduleAtFixedRate(new SleepTimeCalculatorTask(), 1000, 1000);
        }
    }

    public static void main(final String[] args) throws Exception {
        final String[] dummyBidderArguments =
                {DummyBidderDetails.getDumbidPort(), DummyBidderDetails.getDumbidTimeOut(),
                        DummyBidderDetails.getDumbidPercentAds(), DummyBidderDetails.getDumbidBudget(),
                        DummyBidderDetails.getDumbidSeatId(), DummyBidderDetails.getDumbidToggleUnderstress()};
        Main.hostDummyBidder(dummyBidderArguments);
    }
}
