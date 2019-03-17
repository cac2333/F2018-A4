package comp557.a4;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import comp557.a4.IntersectResult;
import comp557.a4.Intersectable;
import comp557.a4.Ray;

/**
 * The scene is constructed from a hierarchy of nodes, where each node
 * contains a transform, a material definition, some amount of geometry, 
 * and some number of children nodes.  Each node has a unique name so that
 * it can be instanced elsewhere in the hierarchy (provided it does not 
 * make loops. 
 * 
 * Note that if the material (inherited from Intersectable) for a scene 
 * node is non-null, it should override the material of any child.
 * 
 */
public class SceneNode extends Intersectable {
	
	/** Static map for accessing scene nodes by name, to perform instancing */
	public static Map<String,SceneNode> nodeMap = new HashMap<String,SceneNode>();
	
    public String name;
   
    /** Matrix transform for this node */
    public Matrix4d M;
    
    /** Inverse matrix transform for this node */
    public Matrix4d Minv;
    
    /** Child nodes */
    public List<Intersectable> children;
    
    /**
     * Default constructor.
     * Note that all nodes must have a unique name, so that they can used as an instance later on.
     */
    public SceneNode() {
    	super();
    	this.name = "";
    	this.M = new Matrix4d();
    	this.Minv = new Matrix4d();
    	this.children = new LinkedList<Intersectable>();
    }
           
    @Override
    public void intersect(Ray ray, IntersectResult result) {

    	// TODO: Objective 7: implement hierarchy with instances
    	
    	//compute transformed eyePoint and viewDirection
    	//affine transformation??w useless??
    	Vector4d row=new Vector4d();
    	Vector4d newPoint=new Vector4d(ray.eyePoint.x,ray.eyePoint.y,ray.eyePoint.z,1);
    	Vector4d newD=new Vector4d(ray.viewDirection.x,ray.viewDirection.y,ray.viewDirection.z,0);
    	
  
    	Point3d p=matrixMul(Minv, newPoint);
    	Point3d temp=matrixMul(Minv, newD);
    	Vector3d d=new Vector3d(temp.x, temp.y, temp.z);
    	Ray newRay=new Ray(p,d);
    	
    	//find the nearest intersection point
    	for(Intersectable i:children){
    		IntersectResult r=new IntersectResult();
    		i.intersect(newRay, r);
    		if (r.t<result.t){
    			result.material=r.material;
    			result.t=r.t;
    			result.n=r.n;
    			result.p=r.p;
    		}
    	}
    	
    	if(result.material==null){
    		result.material=this.material;
    	}
    	
    	//transform normal back
    	Vector4d normal=new Vector4d(result.n.x,result.n.y, result.n.z, 0);
    	Matrix4d MT= new Matrix4d(Minv);
    	MT.transpose();
    	temp=matrixMul(MT, normal);
    	result.n.x=temp.x;
    	result.n.y=temp.y;
    	result.n.z=temp.z;
    	
    	//transform intersection point
    	newPoint=new Vector4d(result.p.x, result.p.y, result.p.z, 0);
    	result.p=matrixMul(M,newPoint );
    	result.t=result.p.distance(ray.eyePoint);
    	
    }
    
    
    private Point3d matrixMul(Matrix4d m, Vector4d v){
    	double x, y, z, w;
    	Vector4d row=new Vector4d();
    	m.getRow(0, row);
    	x=row.dot(v);
    	m.getRow(1, row);
    	y=row.dot(v);
    	M.getRow(2, row);
    	z=row.dot(v);
    	
    	return new Point3d(x, y, z);
    }
    
}
