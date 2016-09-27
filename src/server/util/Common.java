package server.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Common {
	
	public  static String unmarshalling(String input,String attribute) {
		JSONParser parser = new JSONParser();
		JSONObject clientJson;
		try {
			clientJson = (JSONObject) parser.parse(input);

			String type = (String) clientJson.get(attribute);
			return type;



		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
