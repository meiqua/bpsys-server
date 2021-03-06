package dao;

//import java.util.List;

import dao.ItemDao;
import model.Item;
import model.ParseLocation;

import java.util.List;

import common.dao.impl.BaseDaoHibernate4;

public class ItemDaoHibernate4 extends BaseDaoHibernate4<Item>
	implements ItemDao{
	@Override
	public List<Item> findItems(String query)
	{
		//add find by title ��
		
		String[] arr = query.split(" |,");
		
		String myQuery1="";
		String myQuery2="";
		
		for(int i=0;i<arr.length;i++){
			myQuery1=myQuery1+" en.author like '%"+ arr[i].trim()+"%'"+" or ";
			myQuery2=myQuery2+" en.title like '%"+ arr[i].trim()+"%'"+" or ";
		}
		myQuery2=myQuery2.substring(0,myQuery2.length()-3);
		//remove "or " at last
		
		return find("select distinct en from "+"Item"
			 + " en where "+myQuery1.trim()+" "+myQuery2.trim()
			 );
	}
	@Override
	public List<Item> findByLocationMinus(String query) {
        //this is for system, to query state<0
		//get location of shelf
//		String shelf="";
		try {
			//if contain detail location in shelf
			String[] parts = query.split("-");
			if(parts.length > 5){//location contains id
				query=query.substring(0,query.lastIndexOf("-"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return find("select en from "+"Item"
				 + " en "+"where en.location like '"+query.trim()+"%'"
				 + " and en.state<0");
		//find all items in the shelf 
	}
	
	@Override
	public List<Item> findByLocationPlus(String query) {
      //plus is for reader query,should think about state
		//get location of shelf
		//String shelf="";
		try {
			//if contain detail location in shelf
			String[] parts = query.split("-");
			if(parts.length > 5){//location contains id
				query=query.substring(0,query.lastIndexOf("-"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return find("select en from "+"Item"
				 + " en "+"where en.location like '"+query.trim()+"%'"
				 + " and en.state>0");
		//find all items in the shelf 
	}
	
	@Override
	public List<Item> findByState(int state,String head) {
		// find specified item by state
		//head is the shelfLocation
		return find("select en from "+"Item"
				 + " en "+"where en.location like '"+head.trim()+"%'"
				 +" and en.state="+state);
	}
	
	
	@Override
	public Item findByMyId(String id) {
		//for system query
		//find item by id
		List<Item> list= find("select en from "+"Item"
				 + " en "+"where en.id= '"+id.trim()+ "' ");
		//pay attention to space around id can't be ' id '
		
		if(list.size()==0){
			return null;
		}
		else
		return list.get(0);
	}
	
	@Override
	public Item findByMyIdPlus(String id) {
		//plus is for reader query,should think about state
		//find item by id
		
		//but finally I find it does not need to query with state here
		//so now it is same to findByMyId
		List<Item> list= find("select en from "+"Item"
				 + " en "+"where en.id= '"+id.trim()+ "' ");
		//pay attention to space around id can't be ' id '
		if(list.size()==0){
			return null;
		}
		else
		return list.get(0);
	}
		
	@Override
	public void updateForDownItem(Item entity) {
		entity.setState(-1);
		update(entity);
		
		String entityLocation=entity.getLocation();
		List<Item> list=findByLocationPlus(entityLocation);
					
		if(list.contains(entity))
		list.remove(entity);	
		
		int entityLocId=ParseLocation.parseLocationForId(entity.getLocation());
		int tempLocId=-1;
		for(int i=0;i<list.size();i++){
			tempLocId=ParseLocation.parseLocationForId(list.get(i).getLocation());
			if(tempLocId>=entityLocId){
				list.get(i).setLocation(ParseLocation.parseIdForLocation(entityLocation,tempLocId-1));
				update(list.get(i));
			}
		}
	}
	@Override
	public void updateForUpItem(Item entity) {
		// TODO Auto-generated method stub
		String entityLocation=entity.getLocation();
		String entityHead=ParseLocation.parseLocationForHead(entityLocation);
		
		List<Item> list=findByLocationPlus(entityHead);
		if(list.contains(entity))
		list.remove(entity);
		//get list in the same book shelf 
		
		int entityId = ParseLocation.parseLocationForId(entityLocation);
		
		for(int i=0;i<list.size();i++){
			int storedId = ParseLocation.parseLocationForId(list.get(i).getLocation());
			if(storedId >= entityId){
				String updatedLocation=ParseLocation.parseIdForLocation(entityHead, storedId+1);
				list.get(i).setLocation(updatedLocation);
				update(list.get(i));
			}
		}

	}

}
