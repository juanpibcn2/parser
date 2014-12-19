import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Parser {
	private static final boolean LOG_warn = true;
	private static final boolean LOG_info = false;
	private static final boolean LOG_debug = false;
	private final static int numcols = 13;
	private final static char delimiter = ',';
	private final static String def_val = "0";
	private final static String prefix = "essentials.build.";
	public static  HashMap<String, Integer> columns = null;
	public static HashMap<String, Integer> output = null;

	
	
		public static void warn (String s){
			if(LOG_warn)
				System.out.println("[WARN] "+s);
		}
		public static void err (String s){
			System.out.println("[ERROR] "+s);
		}
		public static void info (String s){
			if(LOG_info)
				System.out.println("[INFO] "+s);
		}
		public static void dbg (String s){
			if(LOG_debug)
				System.out.println("[DEBUG] "+s);
		}
		private static String nvl (String s){

			if (s=="")
				return def_val;
			else
				return s;
		}
		private static void init_map(){
			columns = new LinkedHashMap<String, Integer>();
			columns.put("Id",1);
			columns.put("Place",5);
			columns.put("Craft",6);
			columns.put("Interact",7);
			columns.put("Break",8);
			columns.put("Pickup",9);
			columns.put("Drop",10);
			columns.put("AT",11);
			columns.put("AF",12);
			
			output = new LinkedHashMap<String, Integer>();
			output.put("place",0);
			output.put("craft",1);
			output.put("interact",2);
			output.put("break",3);
			output.put("pickup",4);
			output.put("drop",5);
			
			
		}

		private static ArrayList<String> getArray (String line){
			ArrayList <String> l = new ArrayList <String>();
			String s = "";
			for (int i = 0; i< line.length(); ++i){
				if(line.charAt(i)!=delimiter){
					s = s + line.charAt(i);
				}
				else{
					l.add(s);
					s="";
				}
				
			}
			return l;
		}
		public static BufferedReader getReader(String f){
			File file = null;
			try {
				file = new File (f);
			} catch (Exception e) {
				e.printStackTrace();
			}
			FileReader fr = null;
			try {
				fr = new FileReader(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();			
			}
			BufferedReader br = null;
			try{
				br = new BufferedReader(fr);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return br;
		}
		
		public static void main (String [] args) {
			//Initi the HashMaps 
			init_map();
			//Check the number of arguments passed to the app
			if(args.length != 2){
				err("Wrong arg count: "+args.length);
				return;
			}
			//Attempt to open the first arg file to read
			BufferedReader br = null;
			try{
				br = getReader(args[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			PrintWriter out = null;
			try {
			   out = new PrintWriter(new BufferedWriter(new FileWriter(args[1], true)));
			}
			catch (Exception e){
				e.printStackTrace();
			}
			String line = null;
			Boolean header =  true;
			HashMap <String,String> bm = null;
			ArrayList <String> f = null;
			//Read the file pointed by br line by line
			try {
				while ( ( line = br.readLine( ) ) != null) {
					if(!header){
						bm=process(line);
						info(bm.get("Map"));
						if(bm.get("Map").length()!=6 || bm.get("Id").equals("-1")){
							err("FATAL processing: "+line);
							err(bm.toString());
							break;
						}
						f=print(bm.get("Id"),bm.get("Map"));
						write(f,out);
						
					}
					else{
						header = false;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();				
			}
			finally{
				try {
					br.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				br.close();
				out.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		

		
		
		
		private static HashMap <String,String> process (String line){
			HashMap <String,String> ret = new HashMap<String, String>();			
			
			ArrayList <String> l = getArray(line);
			dbg("Processing:" + l.toString());
			//Validate that the id is indeed a number
			if(!l.get(columns.get("Id")).matches("^[0-9]+$")){
				err(l.toString());
				err("Incorrect ID format");
				ret.put("Id", "-1");
				ret.put("Map", "");
				return ret;
			}
			else{
				ret.put("Id",l.get(columns.get("Id")));
			}
			//Check that the line has been correctly split
			if(l.size()!=numcols){
				err("The line does not contain the right amount of columns ("+l.size()+")");
				err(line);
				ret.put("Id", "-1");
				ret.put("Map", "");
				return ret;
				
			}
			//map format [0|1]: PlCIBPiD
			//Default to all false
			String map = "000000";
			
			//Validate a few constraints and show warnings
			
			//All true and all false both equal 1 on the same line
			if(!nvl(l.get(columns.get("AT"))).equals("0") && !nvl(l.get(columns.get("AF"))).equals("0")){
				warn("***All true y all false seleccionados en la misma linea. All false prevalece:***");
				warn(l.toString());
				warn("******");

			}
			//(All true or all false selected) and another node selected on the same line
			else if(!nvl(l.get(columns.get("AT"))).equals("0")   || !nvl(l.get(columns.get("AF"))).equals("0")){	
				if ( !nvl(l.get(columns.get("Place"))).equals("0") ||  !nvl(l.get(columns.get("Craft"))).equals("0") ||
					  !nvl(l.get(columns.get("Interact"))).equals("0") ||  !nvl(l.get(columns.get("Break"))).equals("0") ||
					  !nvl(l.get(columns.get("Pickup"))).equals("0") ||  !nvl(l.get(columns.get("Drop"))).equals("0")){
					warn("*********All true o all false y alguna otra cosa seleccionada. All true y all false prevalcen sobre los especificos***");				warn(l.toString());
					warn(l.toString());
					warn("******");

					
				}
			}
			//If all false we can return 000000
			if(l.get(columns.get("AF")).equals("1")){
				map="000000";
				ret.put("Map", map);
				return ret;
			}
			//If all true we can return 111111
			else if(l.get(columns.get("AT")).equals("1")){
				map = "111111";
				ret.put("Map", map);
				return ret;
			}
			else{
				map ="";
				Iterator<Entry<String, Integer>> it = columns.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry e = (Map.Entry)it.next();
			        if(!e.getKey().equals("Id") && !e.getKey().equals("AT") && !e.getKey().equals("AF") ){
			        	if(nvl(l.get((int)e.getValue())).matches("[0|1]") ){
			        		map = map + nvl(l.get((int)e.getValue()));
			        	}
			        	else{
			        		warn("***"+e.getKey()+" is not a empty, 0 or 1 in:****");
			        		warn(line);
			        		warn("Defaulting to 0");
			        		warn("*******");
			        		map=map+"0";
			        	}
			        }
			    }
			}
			ret.put("Map", map);
			return ret;
		}
		
		
		
		private static ArrayList <String> print (String id, String map){
			ArrayList <String> ret = new ArrayList<String>();
			String s  = "";
			Iterator<Entry<String, Integer>> it = output.entrySet().iterator();
			    while (it.hasNext()) {
			    	s= "";
			        Map.Entry e = (Map.Entry)it.next();
			        if(map.charAt((int) e.getValue()) == '0'){
			        	s="-";
			        }
			        s=s+prefix+e.getKey()+"."+id;
			        info(s);
			        ret.add(s);
			    }
			    info("--------------------");
			    return ret;
		}
		
		private static void write(ArrayList<String> f, PrintWriter out){
			for(int i=0; i< f.size() ; ++i){
				out.write(f.get(i)+"\n");
			}
			
		}
}
		
		
		
		

		
		

