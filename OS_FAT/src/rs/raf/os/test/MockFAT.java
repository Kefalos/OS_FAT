package rs.raf.os.test;

import rs.raf.os.disk.SimpleDisk;
import rs.raf.os.fat.FAT16;
import rs.raf.os.fat.FATException;

public class MockFAT implements FAT16 {

    private int[] cluster;
    private int width; // koliko sektora jedan klaster zauzima
    private int maxCluster = 0xFFED;

    public MockFAT(int clusterWidth) {
	this.width = clusterWidth;
	this.cluster = new int[maxCluster + 2];
    }

    public MockFAT(int clusterWidth, int clusterCount) throws FATException {
	super();
	this.width = clusterWidth;
	if (clusterCount <= maxCluster + 2 && clusterCount >= 2)
	    this.cluster = new int[clusterCount + 2];

	else {
	    throw new FATException("Incorrect number of clusters");
	}
    }

    @Override
    public int getEndOfChain() {
	return 0xFFF8;
    }

    @Override
    public int getClusterCount() {
	return cluster.length - 2;
    }

    @Override
    public int getClusterWidth() {
	return width;
    }

    @Override
    public int readCluster(int clusterID) throws FATException {
	if (clusterID < 2 || clusterID > cluster.length - 1) {
	    throw new FATException("Incorrect cluster ID");
	}

	return cluster[clusterID - 2];
    }

    @Override
    public void writeCluster(int clusterID, int valueToWrite) throws FATException {
	if (clusterID < 2 || clusterID > cluster.length - 1) {
	    throw new FATException("Incorrect cluster ID");
	}
	cluster[clusterID - 2] = valueToWrite;
    }

    @Override
    public String getString() {
	String s = "[";
	for (int i = 0; i < cluster.length-2; i++) {
	    s += String.valueOf(cluster[i]);
	    s += "|";
	}
	s = s.substring(0, s.length() - 1);
	s += "]";
	return s;
    }

    public int[] getCluster() {
        return cluster;
    }

    

}
