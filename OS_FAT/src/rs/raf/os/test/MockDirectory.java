package rs.raf.os.test;

import java.lang.reflect.Array;
import java.util.Hashtable;
import java.util.LinkedList;

import rs.raf.os.dir.Directory;
import rs.raf.os.dir.DirectoryException;
import rs.raf.os.disk.Disk;
import rs.raf.os.disk.SimpleDisk;
import rs.raf.os.fat.FAT16;

public class MockDirectory implements Directory {

    SimpleDisk disk;
    MockFAT fat;
    // kljuc string - naziv fajla
    // vrednost integer - prvi klaster u fatu, clusterID
    Hashtable<String, Integer> fajlovi = new Hashtable<>();
    
    // trebalo mi je na par mesta pa bolje da imam kao atribut
    private int bytesPerCluster;
    
    public MockDirectory(FAT16 fat, Disk disk) throws DirectoryException{
	this.fat = (MockFAT) fat;
	this.disk = (SimpleDisk) disk;
	bytesPerCluster = fat.getClusterWidth() * disk.getSectorSize();
    }

    @Override
    public boolean writeFile(String name, byte[] data) {
	if( fajlovi.containsKey(name)) {
		deleteFile(name);
	}
	if(data.length > getUsableFreeSpace()){
	    return false;
	}
	int prviSlobodan = -1;
	for(int i = 2 ; i < fat.getClusterCount()+2; i++){
	    if(fat.readCluster(i) == 0){
		prviSlobodan = i;
		break;
	    }
	}
	int stariSlobodan = prviSlobodan;
	
	// gde pocinje fajl name
	fajlovi.put(name, prviSlobodan);
	// trazim prvi slobodan klaster
	for(int i = 0; i< data.length; i+= bytesPerCluster){
	    for(int j = prviSlobodan + 1 ; j < fat.getClusterCount() + 2; j++){
		    if(fat.readCluster(j) == 0){
			prviSlobodan = j;
			break;
		    }
		}
	    //upis u fat
	    fat.writeCluster(stariSlobodan, prviSlobodan);
	    
	    // slucaj kada ce biti neka prazna mesta u poslednjem klasteru
	    if(bytesPerCluster + i > data.length){
		//stariSlobodan = prviSlobodan;
		byte[] b = new byte[bytesPerCluster]; //duzine klastera jer treba sve sektore na disku popuniti
		int celiDeo = data.length - (data.length % bytesPerCluster);
		for(int k = 0; k < data.length % bytesPerCluster; k++){
		    b[k] = data[celiDeo + k];
		}
		
		for(int k = data.length % bytesPerCluster + 1; k < b.length; k++){
		    b[k] = 0; // moj kod za popunjavanje praznih mesta
		}
		disk.writeSectors((stariSlobodan-2)*fat.getClusterWidth(), fat.getClusterWidth() , b);
		break;
	    }
	    
	    byte[] b = new byte[bytesPerCluster];
	    for(int k = 0; k<bytesPerCluster;k++){
		    b[k] = data[i+k];
		}

	    disk.writeSectors((stariSlobodan-2)*fat.getClusterWidth(), fat.getClusterWidth(), b);
	    stariSlobodan = prviSlobodan;
	}
	
	// prosao je kroz bajtove, sada u poslednjem klasteru treba da pise EOC
	fat.writeCluster(stariSlobodan, fat.getEndOfChain());
	return true;
	
    }

    @Override
    public byte[] readFile(String name) throws DirectoryException {
	if(!fajlovi.containsKey(name)){
	    throw new DirectoryException("No such file exists!");
	}
	int vrednostSledecegKlastera = fajlovi.get(name);
	int trenutni = vrednostSledecegKlastera;
	LinkedList<Byte[]> listaKlastera = new LinkedList<>();
	//punim listu klaster po klaster
	while(vrednostSledecegKlastera!=fat.getEndOfChain()){
	    vrednostSledecegKlastera = fat.readCluster(vrednostSledecegKlastera); // procitaj sledeci klaster
	    byte[] b = disk.readSectors((trenutni-2)*fat.getClusterWidth(), fat.getClusterWidth());
	    Byte[] c = byteToByte(b);
	    listaKlastera.add(c);
	    trenutni = vrednostSledecegKlastera;
	}
	
	byte[] value = new byte[listaKlastera.size()*bytesPerCluster];
	int i = 0;
	for (Byte[] b : listaKlastera) {
	    byte[] c = ByteToByte(b);
	    System.arraycopy(c, 0, value, i, c.length);
	    i+=c.length;
	}
	
	int j = value.length-1;
	while(value[j--]==0);
	j++;
	byte[] ret = new byte[++j];
	System.arraycopy(value, 0, ret, 0, j);
	return ret;
    }

    private Byte[] byteToByte(byte[] a){
	Byte[] byteObjects = new Byte[a.length];

	int i=0;    
	for(byte b: a)
	   byteObjects[i++] = b;  
	
	return byteObjects;

    }
    private byte[] ByteToByte(Byte[] a){
	byte[] bytes= new byte[a.length];

	int i=0;    
	for(Byte b: a)
	   bytes[i++] = b;  
	
	return bytes;

    }
    @Override
    public void deleteFile(String name) throws DirectoryException {
	if(!fajlovi.containsKey(name)){
	    throw new DirectoryException("No such file exists!");
	}
	int vrednostSledecegKlastera = fajlovi.get(name);
	int trenutni = vrednostSledecegKlastera;
	LinkedList<Byte[]> a = new LinkedList<>();
	while(vrednostSledecegKlastera!=fat.getEndOfChain()){
	    vrednostSledecegKlastera = fat.readCluster(vrednostSledecegKlastera);
	    fat.writeCluster(trenutni, 0);
	    trenutni = vrednostSledecegKlastera;
	}
	fajlovi.remove(name);
    }

    @Override
    public String[] listFiles() {
	String[] s =  new String[fajlovi.keySet().size()];
	String str = fajlovi.keys().nextElement();
	int i = 0;
	while (fajlovi.keys().hasMoreElements()) {
	    s[i++]=str;
	    str = fajlovi.keys().nextElement();
	}
	return s;
    }

    @Override
    public int getUsableTotalSpace() {
	if(disk.diskSize() < fat.getClusterCount()*bytesPerCluster){
	    return disk.diskSize();
	}
	else{
	    return fat.getClusterCount()*bytesPerCluster;
	}
    }

    @Override
    public int getUsableFreeSpace() {
	int count = 0;
	for (int i = 2; i<fat.getClusterCount() + 2; i++) {
	    if(fat.readCluster(i)!=0){
		
		count+=bytesPerCluster;
	    }
	}
	return getUsableTotalSpace()-count;
    }

}
