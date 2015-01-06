/**
 * 출처 : http://stackoverflow.com/questions/3619796/how-to-read-a-properties-file-in-java-in-the-original-order
 * 저자 : Wayne Johnson
 * 참고 : stackoverflow 사이트에서 "Wayne Johnson" 님 일시 Oct 24 '12 at 15:08 에 답글
 */
package kr.pe.sinnori.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("serial")
public class SequencedProperties extends Properties {
	// private Logger log = LoggerFactory.getLogger(SequencedProperties.class);

    
	private List<String> keyList = new ArrayList<String>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public synchronized Enumeration keys() {
        return Collections.enumeration(keyList);
    }

	@Override
    public synchronized Object put(Object key, Object value) {		
        if (! containsKey(key)) {
            keyList.add((String)key);
        }

        return super.put(key, value);
    }

    @Override
    public synchronized Object remove(Object key) {
        keyList.remove(key);

        return super.remove(key);
    }

    @SuppressWarnings("unchecked")
	@Override
    public synchronized void putAll(@SuppressWarnings("rawtypes") Map values) {
        for (Object key : values.keySet()) {
            if (! containsKey(key)) {
                keyList.add((String)key);
            }
        }

        super.putAll(values);
    }
    
    /*@Override
    public void store(OutputStream os, String title) throws IOException {
    	String NEWLINE = System.getProperty("line.separator");
    	
    	
    	Charset charset = Charset.forName("UTF-8");
    	byte[] newLineBytes = NEWLINE.getBytes(charset);    	
    	
    	os.write(title.getBytes(charset));
    	os.write(newLineBytes);
    	
    	byte[] equalsBytes = "=".getBytes(charset); 
    	
    	for (String key : keyList) {
    		String value = this.getProperty(key);
    		String escapeKey = StringEscapeUtils.escapeCsv(key);
    		String escapeValue = StringEscapeUtils.escapeCsv(value);
    		
    		log.info("[{}]=[{}]", key, value);
    		os.write(escapeKey.getBytes(charset));
    		os.write(equalsBytes);
    		
    		os.write(escapeValue.getBytes(charset));
    		os.write(newLineBytes);
    	}
    }*/
}