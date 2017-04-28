PROJECT 3 - Sooji Yi
---------------------

INFO:
---------
Files:
	PingClient.java
	PingServer.java (I modified this only to help debug, and then I reverted the changes)

COMMENTS:
----------
What works:
	> Compiles and starts
	> Sends ping requests over UDP to PingServer
	> Receives replies over UDP from server
	> I implemented the ping message format
	> It times out correctly if reply isn't received (I have it print out nothing as the test cases show)
	> It prints individual statistics as replies are received
	> It prints aggregate packet loss statistics
	> It prints aggregate round trip time statistics

What doesn't work:
	> It does not wait the correct time interval between sending ping requests.

How I tried to fix it:
	I realized that instead of waiting, say, a period of 1000 ms after sending one packet, it would send 1000 ms PLUS the time it took for it to receive a response for that previous packet.

	I knew then that this is where multithreading would be needed. So I created two threads: one for the sending and one for the receiving.

	I got it to sort-of send and receive, but the major problem I encountered was that I couldn't keep track of the individual packet. So if I were running the thread for the receiving packet, I couldn't calculate the individual statistics because I didn't know how to pass in the data from a specific packet being sent and matching that with whatever packet was being received, on a separate thread.

	I had both threads running and they were classes that extended Thread, and I considered having them both have functions that return an array list of times sent or received for each packet, but I realized then that there would be a delay in printing (so it would not print individual stats as replies are received).

	Also, I couldn't keep track of the sequence number and count without it getting messy with the constructor and passing it in for each thread.

	Ultimately, I couldn't find an adequate solution that would fulfill all (or most) of the requirements, so I reverted back to this solution that I have submitted, which fulfills all of the conditions except for the one.

	I have also included my attempt at multithreading in a comment after all of my active code in PingClient.