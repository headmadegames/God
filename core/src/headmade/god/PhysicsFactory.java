package headmade.god;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import headmade.god.actors.SolarSystem;
import net.dermetfan.gdx.graphics.g2d.Box2DSprite;
import util.RandomUtil;

public class PhysicsFactory {

	private static final String	TAG	= PhysicsFactory.class.getName();

	private World				world;
	private Shape				circleShape;

	public PhysicsFactory(World world) {
		this.world = world;
		circleShape = new CircleShape();
	}

	public Body createPlanet(float radius) {
		final BodyDef def = new BodyDef();
		def.type = BodyType.DynamicBody;
		final Body body = world.createBody(def);
		final FixtureDef fixDef = new FixtureDef();
		circleShape.setRadius(radius);
		fixDef.shape = circleShape;
		fixDef.density = 100.1f;
		final Fixture fix = body.createFixture(fixDef);

		final Box2DSprite planetSprite = new Box2DSprite(Assets.instance.skin.getRegion(RandomUtil.random(Assets.txPlanets)));
		planetSprite.setColor(MathUtils.random(0.75f, 1.0f), MathUtils.random(0.6f, 0.8f), MathUtils.random(0.75f, 1.0f), 1f);
		fix.setUserData(planetSprite);
		body.setUserData(planetSprite);
		return body;
	}

	public Body createPlayer() {
		final BodyDef def = new BodyDef();
		def.type = BodyType.DynamicBody;
		final Body body = world.createBody(def);
		final FixtureDef fixDef = new FixtureDef();
		circleShape.setRadius(0.01f);
		fixDef.shape = circleShape;
		fixDef.density = 1.0f;
		body.createFixture(fixDef);
		return body;
	}

	public SolarSystem createSolarSystem(float... planetsRadius) {
		final float gravConst = 10f;

		float maxRadius = 0.001f;
		final Array<Body> planets = new Array<Body>();
		for (int i = 0; i < planetsRadius.length; i++) {
			planets.add(createPlanet(planetsRadius[i]));
			maxRadius = Math.max(maxRadius, planetsRadius[i]);
		}

		final float solRadius = maxRadius * 10;
		for (int i = 0; i < planets.size; i++) {
			final float solDist = solRadius + solRadius * (i + 1);
			final float rotDeg = MathUtils.random(360);
			final Vector2 planetVec = new Vector2(solDist, 0).rotate(rotDeg);

			final float orbitVel = new Float(Math.sqrt(gravConst * planets.get(i).getMass() / solDist));
			Gdx.app.log(TAG, "planets.get(i).getMass() " + planets.get(i).getMass() + " orbitVel " + orbitVel);
			// if (i % 2 == 0) {
			// planets.get(i).setTransform(solDist, 0, 0f);
			// planets.get(i).setLinearVelocity(new Vector2(0f, orbitVel));// .nor().scl(orbitVel));
			// } else {
			// planets.get(i).setTransform(-solDist, 0, 0f);
			// planets.get(i).setLinearVelocity(new Vector2(0f, -orbitVel));// .nor().scl(orbitVel));
			// }

			planets.get(i).setTransform(planetVec.x, planetVec.y, 0f);
			planets.get(i).setLinearVelocity(new Vector2(0f, orbitVel).rotate(rotDeg));// .nor().scl(orbitVel));
		}

		final BodyDef def = new BodyDef();
		def.type = BodyType.StaticBody;
		final Body sun = world.createBody(def);
		final FixtureDef fixDef = new FixtureDef();
		Gdx.app.log(TAG, "maxRadius " + maxRadius);
		circleShape.setRadius(solRadius);
		fixDef.shape = circleShape;
		// fixDef.density = 100f;
		final Fixture sunFix = sun.createFixture(fixDef);

		final Box2DSprite sunSprite = new Box2DSprite(Assets.instance.skin.getRegion(Assets.txSun));
		sunFix.setUserData(sunSprite);
		sun.setUserData(sunSprite);

		final SolarSystem solarSystem = new SolarSystem(sun, planets);
		solarSystem.setGravConst(gravConst);
		return solarSystem;
	}

	public void dispose() {
		circleShape.dispose();
	}
}
