package at.theengine.android.simple_rss2_android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;
import android.widget.ArrayAdapter;

import java.util.regex.Pattern;
import android.util.Log;

public class SimpleRss2Parser extends SimpleFeedParser {

	private SimpleRss2ParserCallback mCallback;
	
    public SimpleRss2Parser(String feedUrl, SimpleRss2ParserCallback callback) {
        super(feedUrl);
        this.mCallback = callback;
    }
    
    public void parseAsync(){
    	AsyncTask task = new AsyncTask() {

			private Exception mEx;
			private List<RSSItem> items;
			
			@Override
			protected void onPostExecute(Object result) {
				if(mEx != null){
					if(mCallback != null){
						mCallback.onError(mEx);
					}
				} else {
					if(mCallback != null){
						mCallback.onFeedParsed(items);
					}
				}
			}
			
			@Override
			protected Object doInBackground(Object... arg0) {
				try {
					items = parse();
				} catch(Exception e){
					mEx = e;
				}
				
				return null;
			}
		};
		
		task.execute();
    }

    public List<RSSItem> parse() {
        final RSSItem currentMessage = new RSSItem();
        RootElement root = new RootElement("rss");
        final List<RSSItem> messages = new ArrayList<RSSItem>();
        Element channel = root.getChild("channel");
        Element item = channel.getChild(ITEM);
        item.setEndElementListener(new EndElementListener(){
            public void end() {
                messages.add(currentMessage.copy());
            }
        });
        item.getChild(TITLE).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentMessage.setTitle(body);
            }
        });
        item.getChild(LINK).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentMessage.setLink(body);
            }
        });
        item.getChild(DESCRIPTION).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentMessage.setDescription(body);
            }
        });
        item.getChild("http://purl.org/rss/1.0/modules/content/", "encoded").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentMessage.setContent(body);
            }
        });
        item.getChild(CONTENT).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentMessage.setContent(body);
            }
        });
        item.getChild(PUB_DATE).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentMessage.setDate(body);
            }
        });
	    
	Pattern patternIso = Pattern.compile("ISO-8859-1");
        Pattern utf = Pattern.compile("UTF-8");
        Pattern utf16 = Pattern.compile("UTF-16");
        Pattern usascii = Pattern.compile("US-ASCII");
	    
        if(patternIso.matcher(this.getInputStream().toString()).find()) {
        	try {
                	Xml.parse(this.getInputStream(), Xml.Encoding.ISO_8859_1, root.getContentHandler());
                	Log.d("parse()", "ISO-8859-1 Encoding");
                	return messages;
			
		} catch (Exception var7) {
                	throw new RuntimeException(var7);
                }
        } else if(utf16.matcher(this.getInputStream().toString()).find()) {
         	try {
                	Xml.parse(this.getInputStream(), Xml.Encoding.UTF_16, root.getContentHandler());
                        Log.d("parse()", "UTF-16 Encoding");
                        return messages;
			
		} catch (Exception var7) {
                        throw new RuntimeException(var7);
                }
        } else if(usascii.matcher(this.getInputStream().toString()).find()) {
                try {
                	Xml.parse(this.getInputStream(), Xml.Encoding.US_ASCII, root.getContentHandler());
                        Log.d("parse()", "US-ASCII Encoding");
                        return messages;

                } catch (Exception var7) {
                	throw new RuntimeException(var7);
                }
        } else {
                try {
                	Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
                        Log.d("parse()", "UTF-8 Encoding");
                        return messages;
			
                } catch (Exception var7) {
                	throw new RuntimeException(var7);
                }
	}
        return messages;
    }
}
