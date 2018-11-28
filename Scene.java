package comp557.a4;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector3d;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {

	/** List of surfaces in the scene */
	public List<Intersectable> surfaceList = new ArrayList<Intersectable>();

	/** All scene lights */
	public Map<String,Light> lights = new HashMap<String,Light>();


	/** Contains information about how to render the scene */
	public Render render;

	/** The ambient light colour */
	public Color3f ambient = new Color3f();

	private Matrix3d camT;
	private final static double SHADOW_ERROR=1e-5;

	/** 
	 * Default constructor.
	 */
	public Scene() {
		this.render = new Render();
	}

	/**
	 * renders the scene
	 */
	public void render(boolean showPanel) {


		Camera cam = render.camera; 
		int w = cam.imageSize.width;
		int h = cam.imageSize.height;

		render.init(w, h, showPanel);
		camFrame(cam);


		for ( int i = 0; i < h && !render.isDone(); i++ ) {
			for ( int j = 0; j < w && !render.isDone(); j++ ) {

				// TODO: Objective 1: generate a ray (use the generateRay method)

				// TODO: Objective 2: test for intersection with scene surfaces
				
				Ray ray=new Ray();
				generateRay(i, j, new double [] {-.5d,.5d}, cam ,ray);
				
				Color3f c = new Color3f(render.bgcolor);
				IntersectResult near=new IntersectResult();
				Intersectable intersect=intersect(near, ray);;
			
				// TODO: Objective 8: do antialiasing by sampling more than one ray per pixel
				if(intersect!=null){
//					if(render.samples==1){
						if(near.material.specular.x==1&&near.material.specular.y==1&&near.material.specular.z==1){
							c=mirrorReflection(4, near, ray, intersect);
						}else{
							c=shading(near, ray, intersect);
						}
//					}else{
						//c=superSampling(i, j, cam, render.samples, intersect);
//					}
				}
				// update the render image


					int r = (int)(255*c.x);
					int g = (int)(255*c.y);
					int b = (int)(255*c.z);

					int a = 255;
					int argb = (a<<24 | r<<16 | g<<8 | b);
				render.setPixel(j, i, argb);
			}
		}

		// save the final render image
		render.save();

		// wait for render viewer to close
		render.waitDone();

	}
	
	private Intersectable intersect(IntersectResult near, Ray ray){
		
		Intersectable intersect=null;
		//compute intersection
		for(Intersectable s: surfaceList){	
			IntersectResult result=new IntersectResult();
			s.intersect(ray, result);	
			
			if(result.material!=null){
				//	result.t=result.p.distance(ray.eyePoint);
				if(result.t<near.t&&result.t!=0){
					near.p=result.p;
					near.n=result.n;
					near.material=result.material;
					near.t=result.t;
					intersect=s;
				}
			}
		}
		return intersect;
	}
	
	
	private Color3f superSampling(double i, double j, Camera cam, int samples,  Intersectable s){
		
		Color3f c=new Color3f();
		int grid=(int) Math.sqrt(samples);
		double length=1/grid;
		
		for(int n=0; n<grid; n++){
			for (int k = 0; k < grid; k++) {
				Ray ray = new Ray();
				IntersectResult result = new IntersectResult();
				//random sampling within a pixel 
				double rand = Math.random();
				i = Math.max(0, i - .5 + (n+rand)*length);
				rand = Math.random();
				j = Math.max(0, j - .5 + (k+rand)*length);
				generateRay(i, j, new double[] { -.5d, .5d }, cam, ray);
				//compute pixel color
				s.intersect(ray, result);
				if (result.material != null) {
					c.add(shading(result, ray, s));
				} 
			}
		}
		
		//c.scale(1/samples);
		c.x=c.x/samples;
		c.y=c.y/samples;
		c.z=c.z/samples;
		c.clamp(0, 1);
		return c;
	}

	private void camFrame(Camera cam){
		Point3d e=new Point3d(cam.from);

		//camera transformation matrix
		Vector3d lookAt=new Vector3d();
		lookAt.sub(e, cam.to);
		lookAt.normalize();
		Vector3d U=new Vector3d(cam.up);
		U.cross(lookAt, U);
		
		U.normalize();
		Vector3d V =new Vector3d();
		V.cross(lookAt, U);

		camT=new Matrix3d();
		camT.setColumn(0, U);
		camT.setColumn(1, V);
		camT.setColumn(2, lookAt);
	}
	/**
	 * Generate a ray through pixel (i,j).
	 * 
	 * @param i The pixel row.
	 * @param j The pixel column.
	 * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
	 * @param cam The camera.
	 * @param ray Contains the generated ray.
	 */
	public void generateRay(final double i, final double j, final double[] offset, final Camera cam, Ray ray) {

		// TODO: Objective 1: generate rays given the provided parmeters
		double aspect=cam.imageSize.getHeight()/cam.imageSize.getWidth();

		//width of the scene
		//height of the scene ???
		double focal=cam.from.distance(cam.to);
		double t=(focal)*Math.tan(Math.toRadians(cam.fovy/2));
		double r=t/aspect;

		double l = -r;
		double b = -t;
		//convert pixel coordinates to image coordinates
		double u=(double) l+(r-l)*(j+0.5)/cam.imageSize.width;
		double v=(double) b+(t-b)*(i+0.5)/cam.imageSize.height;
		//from camera frame to world frame
		//compute ray function
		Vector3d r0=new Vector3d();
		Vector3d r1=new Vector3d();
		Vector3d r2=new Vector3d();

		camT.getRow(0, r0);
		camT.getRow(1, r1);
		camT.getRow(2, r2);
		
		r0.scale(u);
		r1.scale(v);
		r2.scale(-focal);
		r0.add(r1);
		r0.add(r2);
		r0.add(cam.from);

		Point3d s=new Point3d(r0.x,r0.y,r0.z);
		Vector3d vec=new Vector3d();
		vec.sub(s, cam.from);
		vec.normalize();
		ray.set(cam.from, vec);

	}

	private Color3f diffuseShading(Light l, IntersectResult result){

		//light direction
		Vector3d direc=new Vector3d();
		direc.sub(l.from, result.p);
		double angle=direc.angle(result.n);
		angle=Math.max(0, Math.cos(angle));

		float r=(float) (l.power*result.material.diffuse.x*angle);
		float g=(float) (l.power*result.material.diffuse.y*angle);
		float b=(float) (l.power*result.material.diffuse.z*angle);

		Color3f color=new Color3f(r, g, b);
		return color;

	}

	//this is diffuse shading with an extra ambient term
	private Color3f ambientShading( IntersectResult result){

		//c=cr*ca
		float r=(float) (result.material.diffuse.x*this.ambient.x);
		float g=(float) (result.material.diffuse.y*this.ambient.y);
		float b=(float) (result.material.diffuse.z*this.ambient.z);

		Color3f color=new Color3f(r, g, b);

		return color;	

	}

	private Vector3d reflect(Point3d from, IntersectResult result){
		Vector3d direc=new Vector3d();
		direc.sub(from, result.p);
		direc.normalize();
		
		Vector3d r=new Vector3d(result.n);
		r.scale(2*r.dot(direc));
		r.sub(direc);
		return r;		
	}
	
	private Color3f phongShading(Light l, IntersectResult result, Ray ray){
	//bisector
		Vector3d h=reflect(l.from, result);
		Material mat=result.material;

		Vector3d e=new Vector3d(ray.viewDirection);
		e.negate();
		e.normalize();

		
		double viewAngle=e.angle(h);
		viewAngle=Math.cos(viewAngle);
		
		Color3f shading = new Color3f();
		shading.x += (float)(l.power * mat.specular.x * Math.pow(Math.max(0, viewAngle), mat.shinyness));
		shading.y += (float)(l.power * mat.specular.y * Math.pow(Math.max(0, viewAngle), mat.shinyness));
		shading.z += (float)(l.power * mat.specular.z * Math.pow(Math.max(0, viewAngle), mat.shinyness));
		
		return shading;
	}

	private Color3f shading(IntersectResult result, Ray ray, Intersectable i){

		Color3f color=ambientShading(result);
		
		//convert the Intersectable to SceneNode and test for the shadow
		SceneNode root;
		if(!(i instanceof SceneNode)){
			root=toSceneNode(i);
		}else{
			root=(SceneNode) i;
		}
		
		//adding color component for all light sources 
		for(Light l: lights.values()){
			IntersectResult shadow=new IntersectResult();

			//if in shadow, skip diffuse and Phong shading
			Ray shadowRay=new Ray();
			if(inShadow(result, l, root, shadow, shadowRay)){
				continue;
			}
			Color3f dif=diffuseShading(l, result);
			Color3f pho=phongShading(l, result, ray);
			color.add(dif);
			color.add(pho);
		}

		//make sure color is inside valid range 
		color.clamp(0, 1);
		return color;
	}

	private Color3f mirrorReflection(int max, IntersectResult result, Ray ray, Intersectable i){
		
		if(max>1&&result.material.specular.x==1&&result.material.specular.y==1&&result.material.specular.z==1){
			//reflect ray
			Ray reflect=new Ray();
			reflect.viewDirection=reflect(ray.eyePoint, result);
			reflect.eyePoint=result.p;
			//intersect the reflect ray
			IntersectResult near=new IntersectResult();
			Intersectable newi=intersect(near, reflect);
			
			if(newi==null){
				return shading(result, ray, i);
			}
			Color3f c=mirrorReflection(max-1, near, reflect, newi);
			return c;
		}else{
			return shading(result, ray, i);
		}
	}
	
	private SceneNode toSceneNode(Intersectable i){
		SceneNode node=new SceneNode();
		node.children.add(i);
		Matrix4d identity=new Matrix4d( 1, 0, 0, 0, 
										0, 1, 0, 0, 
										0, 0, 1, 0,
										0, 0, 0, 1);
		node.M=identity;
		node.Minv=identity;
		return node;
	}
	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
	 * @param result Intersection result from raytracing. 
	 * @param light The light to check for visibility.
	 * @param root The scene node.
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise. 
	 */
	public static boolean inShadow(final IntersectResult result, final Light light, final SceneNode root, IntersectResult shadowResult, Ray shadowRay) {

		// TODO: Objective 5: check for shadows and use it in your lighting computation
		
		//shadow ray
		Point3d p=result.p;
		Vector3d lightDirection=new Vector3d(p);
		lightDirection.sub(light.from);
//		lightDirection.negate();
		shadowRay=new Ray(light.from, lightDirection);
		
		//assign the lightResult the current intersection point distance
		shadowResult.t=result.p.distance(light.from);
		
		//compute ray intersection of every objects
		//if something blocks the light in the middle of the lightRay, return is in shadow
		IntersectResult temp=new IntersectResult();
		for(Intersectable i:root.children){
			i.intersect(shadowRay, temp);
			if(temp.t+SHADOW_ERROR<shadowResult.t){
				return true;
			}
		}
		

		return false;
	}    
}
