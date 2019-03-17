package comp557.a4;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import comp557.a4.PolygonSoup.Vertex;

public class Mesh extends Intersectable {
	
	/** Static map storing all meshes by name */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();
	
	/**  Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";
	private Map<Plane, int[]> map=new HashMap<>();
	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}			
		
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		// TODO: Objective 9: ray triangle intersection for meshes
		IntersectResult near=new IntersectResult();
		
		for(Plane p: map.keySet()){
			IntersectResult temp=new IntersectResult();
			//check if on the plane
			p.intersect(ray, temp);
			if(temp.t<near.t){
				if(inTriangle (temp.p, p)){
					near=temp;
				}
			}
		}
		if(near.t==Double.POSITIVE_INFINITY){
			return;
		}else{
			result.n=near.n;
			result.p=near.p;
			result.t=near.p.distance(ray.eyePoint);
			result.material=this.material;
		}
	}
	
	private boolean inTriangle(Point3d x, Plane p){

		int[] index=map.get(p);
		Point3d p0=soup.vertexList.get(index[0]).p;
		Point3d p1=soup.vertexList.get(index[1]).p;
		Point3d p2=soup.vertexList.get(index[2]).p;
		Vector3d n=p.n;
		
		//test for side p0p1
		if(!side (p0, p1, x, n))
			return false;
		//test for side p1p2
		if(!side (p1, p2, x, n))
			return false;
		//test for side p2p0
		if(!side (p2, p0, x, n))
			return false;

		return true;
	}
	
	private boolean side(Point3d a, Point3d b, Point3d x, Vector3d n){
		
		Vector3d side=new Vector3d();
		Vector3d temp=new Vector3d();
		Vector3d cross=new Vector3d();
		
		//test for side ab
		side.sub(b, a);
		temp.sub(x,a);
		cross.cross(side, temp);
		if(cross.dot(n)>0){
			return true;
		}else{
			return false;
		}
		
	}
	
	public Plane plane(Point3d p0,Point3d p1, Point3d p2 ){
		Vector3d v1 = new Vector3d();
        Vector3d v2 = new Vector3d();
        Vector3d n=new Vector3d();
        v1.sub(p1,p0);
        v2.sub(p2,p1); 
        n.cross( v1,v2 );
        n.normalize();
        return new Plane(p0, n);
	}

	public void planes(){
		
		for(int[] list:soup.faceList){
			Plane p=plane(soup.vertexList.get(list[0]).p,soup.vertexList.get(list[1]).p, soup.vertexList.get(list[2]).p );
			map.put(p, list);
		}
	}
	
}