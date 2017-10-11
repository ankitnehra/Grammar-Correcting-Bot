//Used help from http://staticvoidgames.com/tutorials/howTo/twitterBot

//imports from twitter4j (a Twitter API for Java)
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.*;

public class TweetingBot {
	
	//Map which will store errors and their corrections
	protected static Map<String, String> errorMap;
	
	//List for storing different tweet styles
	protected static List<String> style;
	
	protected static List<Long> alreadyDone;

	static 
	{
		//initialize and populate style
		style = new ArrayList<String>();
		style.add("ERROR! It's ");
		style.add("I am supposing you meant ");
		style.add("Hey! Please don't make my job harder. It's ");
		style.add("You've been caught red-handed by the Grammar Police. It's ");
		style.add("Dare I say you're wrong? It's ");
		style.add("Wrong grammar means war! It's ");
		//style.add("Stop tweeting and pick up a book, would you? It's ");
		//style.add("Are you sure that you're using English? It's ");
		//style.add("I don't care if you use wrong grammar. Said no grammar cop ever! It's ");
		//style.add("Your knowledge of grammar (or lack of it) is annoying. It's ");
		//style.add("Could you please improve your grammar. Following me could help. It's ");
		//style.add("This is the reason why we, the robots, will soon take over. It's ");
		style.add("Please correct. It's ");
		style.add("Kindly correct. It's ");
		
		//initialize and populate errorMap
		errorMap = new HashMap<String, String>();
		errorMap.put("\"should of\"", "\"should have\" or \"should've\".");
		errorMap.put("\"better then\"", "\"better than\".");
		errorMap.put("\"your a\"", "\"you're a\" or \"you are a\".");
		errorMap.put("\"your the\"", "\"you're the\" or \"you are the\".");
		errorMap.put("\"its a\"", "not \"its a\" but \"it's a\".");
		errorMap.put("\"effecting me\"", "\"affecting me\".");
		errorMap.put("\"effecting us\"", "\"affecting us\".");
		errorMap.put("\"its the\"", "not \"its the\" but \"it's the\".");
		errorMap.put("\"alot\"", "\"a lot\".");
		errorMap.put("\"could of\"", "\"could have\" or \"could've\".");
		errorMap.put("\"would of\"", "\"would have\" or \"would've\".");
		
		alreadyDone = new ArrayList<Long>();
	}
	
	//Function to get the authentication for twitter account from a text file
	//@param: String containing file path
	//output: string array containing credentials
	public static String[] getAuth(String filename) throws IOException
	{
		
		BufferedReader in = null;
		String[] authArray = new String[4];
		
		//read text file and store credentials in authArray
		//each line in file should contain a different key/secret etc in the given order
		try
		{	
			in = new BufferedReader(new FileReader(filename));
			String line = in.readLine();
			int count = 0;
			try
			{
				while (count<authArray.length)
				{
					authArray[count]=line;
					line = in.readLine();
					count++;
				}
			}
			//catch end of file exception
			catch (EOFException w)
			{
				w.getStackTrace();
			}
			
		}
		
		//catch file not found exception
		catch (FileNotFoundException e)
		{
			e.getStackTrace();
		}
		
		return authArray;
	}
	
	//Finds errors and replies to them
	//@param: Twitter object
	public static void replyToErrors(Twitter twitter) throws TwitterException
	{
		int numCheckPrevious = 10;
		//An arrayList for all the keys of the map (errors)
		List<String> errors = new ArrayList<String>(errorMap.keySet());
		
		//this will find a tweet which contains one of the errors
		//but the error is chosen randomly from the key set of errrorMap
		int randomIndex = (int)(errors.size()*Math.random());
		String randomKey = errors.get(randomIndex);
		Query query = new Query(randomKey);
		QueryResult result = twitter.search(query);
		
		//Find the first tweet in the results
		Status tweetResult = result.getTweets().get(0);

		
		//check if this not a retweet
		while (tweetResult.isRetweet() || alreadyDone.contains(tweetResult))
		{		//this will find a tweet which contains one of the errors
			//but the error is chosen randomly from the key set of errrorMap
			randomIndex = (int)(errors.size()*Math.random());
			randomKey = errors.get(randomIndex);
			query = new Query(randomKey);
			result = twitter.search(query);
			
			//Find the first tweet in the results
			tweetResult = result.getTweets().get(0);
		}
		//create the tweet with the correction and a randomized style
		StatusUpdate statusUpdate = new StatusUpdate(".@"+tweetResult.getUser().getScreenName()+" "+
				style.get((int)(style.size()*Math.random()))+errorMap.get(randomKey));
		
		//the tweet should reply to the tweet containing error
		statusUpdate = statusUpdate.inReplyToStatusId(tweetResult.getId());
		
		//reply to the error by tweeting
		Status status = twitter.updateStatus(statusUpdate);
		
		//add the tweet to the alreadyDone arraylist to avoid duplicate error finding
		if (alreadyDone.size()==numCheckPrevious)
		{
			alreadyDone.remove(numCheckPrevious);
		}
		alreadyDone.add(tweetResult.getId());

	}
	
    //if something goes wrong, we might see a TwitterException, InterruptedException or IOException
    public static void main(String... args) throws TwitterException, InterruptedException, IOException
    {
    	//The path of the file which contains credentials
    	String infile = "C:\\Users\\ankit.nehra\\Desktop\\TweetingBot\\authFile.txt";
    	String[] authArray = getAuth(infile);
    	
    	//Configure authorization
    	ConfigurationBuilder cb = new ConfigurationBuilder();
    	cb.setDebugEnabled(true)
    	  .setOAuthConsumerKey(authArray[0])
    	  .setOAuthConsumerSecret(authArray[1])
    	  .setOAuthAccessToken(authArray[2])
    	  .setOAuthAccessTokenSecret(authArray[3]);
    	
    	//get a new Twitter object
    	TwitterFactory tf = new TwitterFactory(cb.build());
    	Twitter twitter = tf.getInstance();
	
        //Keep tweeting
        while (true)
        {
        	try
        	{
        		//Find and reply to a tweet
            	replyToErrors(twitter);
            	
            	//Print tweeted everytime it's done replying
            	//may not have replied if tweet found was a retweet
                System.out.println("Tweeted");
        	}
        	
        	catch(TwitterException e)
        	{
        		e.printStackTrace();
        		System.out.println("Couldn't tweet.");
        	}
        	
            finally
            {
            	//Go to sleep for 30 minutes so as to not exceed the max number of tweets in a day limit
            	Thread.sleep(30*60*1000);
            }
        }    
    }
}