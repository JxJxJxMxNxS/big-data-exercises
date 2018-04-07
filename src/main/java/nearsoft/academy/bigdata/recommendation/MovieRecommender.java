package nearsoft.academy.bigdata.recommendation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;


import org.apache.mahout.cf.taste.common.TasteException;
//import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;

import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import static java.lang.Math.toIntExact;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


import java.util.List;




public class MovieRecommender 
{

    private String inputFile;
    private static final String mahoutInput = "mahoutIn.csv";
	private HashMap<String, Integer> products = new HashMap<String, Integer>();
	private HashMap<Integer, String> productsRev = new HashMap<Integer, String>();
	private HashMap<String, Integer> users = new HashMap<String, Integer>();
	private int rvw=0;
	

    public MovieRecommender(String path)
    {
		this.inputFile=path;
		unzip();
	}
	
	public long getTotalReviews()
	{
		return rvw;
	}

	public long getTotalProducts()
	{
		return products.size();
	}

	public long getTotalUsers()
	{
		return users.size();
	}

    public boolean unzip() 
    {
		String line;
		String value;
		String csvLine="";
		
		try
		{
			GZIPInputStream stream = new GZIPInputStream(new FileInputStream(inputFile));
			PrintWriter newFile = new PrintWriter(mahoutInput);
			InputStreamReader reader = new InputStreamReader(stream);
			BufferedReader in = new BufferedReader(reader);
			
			while ((line = in.readLine()) != null) 
			{
				if(line.contains("productId:"))
				{
					value=line.split(":")[1];
					products.put(value,(products.get(value)==null) ? new Integer(products.size()+1) : new Integer(products.get(value)));
					productsRev.put(products.get(value),value);
					rvw++;
					csvLine+=Integer.toString(products.get(value))+",";
				}
				if(line.contains("review/userId:"))
				{
					value=line.split(":")[1].trim();
					users.put(value,(users.get(value)==null) ? new Integer(users.size()+1) : new Integer(users.get(value)));
					csvLine=Integer.toString(users.get(value))+","+csvLine;
				}
				if(line.contains("review/score:"))
				{
					value=line.split(":")[1];
					csvLine+=value.trim()+"\n";
					newFile.write(csvLine);
					csvLine="";
				}	
			}
			stream.close();
			newFile.close();
			return true;
		}
		catch(IOException ex)
		{
			ex.printStackTrace();   
			return false;
		} 
   } 

    

   public List<String> getRecommendationsForUser(String userId)
   {
		int id = users.get(userId);
		List<String> recommendationsList = new ArrayList<String>();
		
		try 
		{
			DataModel model;
			model = new FileDataModel(new File(mahoutInput));
			UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
			UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
			UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
			
			List<RecommendedItem> recommendations = recommender.recommend(id, 3);
			for (RecommendedItem recommendation : recommendations) 
			{
				recommendationsList.add(productsRev.get(toIntExact(recommendation.getItemID())).trim());	
			}
			return recommendationsList;
			
		} 
		catch (IOException e) 
		{
			
			e.printStackTrace();
			return null;
		}
		catch (TasteException e) 
		{
			e.printStackTrace();
			return null;
		}
   }

}
