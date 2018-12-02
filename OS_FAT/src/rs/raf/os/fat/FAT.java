package rs.raf.os.fat;

public class FAT implements FAT16 {
    
    private int cluster;
    private int width; // koliko sektora jedan klaster zauzima
    private int maxCluster = 0xFFED;
    

    public FAT(int cluster, int width) throws FATException{
	super();
	this.width = width;
	if(width <= 0xFFED && width>=2)
	    this.cluster = cluster;
	    
	else{
	    throw new FATException("Incorrect number of clusters");
	}
    }

    @Override
    public int getEndOfChain() {
	return 0xFFF8;
    }

    @Override
    public int getClusterCount() {
	return cluster;
    }

    @Override
    public int getClusterWidth() {
	return width;
    }

    @Override
    public int readCluster(int clusterID) throws FATException {
	if(clusterID <2 || clusterID > maxCluster+2){
	    throw new FATException("Incorrect cluster ID");
	}
	
	return 0;
    }

    @Override
    public void writeCluster(int clusterID, int valueToWrite) throws FATException {
	// TODO Auto-generated method stub

    }

    @Override
    public String getString() {
	// TODO Auto-generated method stub
	return null;
    }

}
