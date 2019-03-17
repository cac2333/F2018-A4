package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple sphere class.
 */
public class Sphere extends Intersectable {
    
	/** Radius of the sphere. */
	public double radius = 1;
    
	/** Location of the sphere center. */
	public Point3d center = new Point3d( 0, 0, 0 );
    
    /**
     * Default constructor
     */
    public Sphere() {
    	super();
    }
    
    /**
     * Creates a sphere with the request radius and center. 
     * 
     * @param radius
     * @param center
     * @param material
     */
    public Sphere( double radius, Point3d center, Material material ) {
    	super();
    	this.radius = radius;
    	this.center = center;
    	this.material = material;
    }
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // TODO: Objective 2: intersection of ray with sphere
    		double squareLength=ray.viewDirection.lengthSquared();
    		double radius=this.radius;
    		Point3d center=this.center;
    		
    		//compute the root of expression
    		double a=squareLength;
    		//b=(P-Q)*t
    		Vector3d vec=new Vector3d();
    		vec.sub(ray.eyePoint, center);
    		Vector3d t=ray.viewDirection;
    		double b=-vec.dot(t);
    		
    		//delta
    		double delta=b*b-(vec.lengthSquared()-radius*radius)*a;
    		
    		
    		if(delta<0){
    			return;
    		}else{
    			double root1=(b+Math.sqrt(delta))/a;
    			double root2=(b-Math.sqrt(delta))/a;
    			
    			//get a point out side of the sphere 
    			if (root1<=0&&root2<=0){
    				return;
    			}
    			
    			result.material=this.material;
    			
    			if(root1>root2&&root2>0){
    				ray.getPoint(root2, result.p);
    				result.n=normal(this, result.p);
    				
    			}else if(root1>root2&&root2<=0){
    				ray.getPoint(root1, result.p);
    				result.n=normal(this, result.p);
    			}else if(root1<root2&&root1>0){
    				ray.getPoint(root1, result.p);
    				result.n=normal(this, result.p);
    			}else{
    				ray.getPoint(root2, result.p);
    				result.n=normal(this, result.p);
    			}
    		}
    		result.t=result.p.distance(ray.eyePoint);
    }
    
    
    private Vector3d normal(Sphere s, Point3d p){
    	Vector3d n=new Vector3d();
    	n.sub(p, s.center);
    	n.normalize();
    	return n;
    }
	
    
    
}
