package headmade.god;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Align;

import headmade.god.actors.SolarSystem;
import net.dermetfan.gdx.graphics.g2d.Box2DSprite;
import net.dermetfan.gdx.physics.box2d.Box2DUtils;

public class SolarSystemScreen implements Screen {
	private static final String	TAG							= SolarSystemScreen.class.getName();

	public final static int		VELOCITY_ITERS				= 3;
	public final static int		POSITION_ITERS				= 2;
	public final static int		MAX_FPS						= 60;
	public final static int		MIN_FPS						= 15;
	public final static float	MAX_STEPS					= 1f + MAX_FPS / MIN_FPS;
	public final static float	TIME_STEP					= 1f / MAX_FPS;
	public final static float	UNIT_SCALE					= 1f / 4f;

	public boolean				debugEnabled				= true;

	OrthographicCamera			camPix;
	OrthographicCamera			cam;
	Body						player;

	private God					god;
	private SolarSystem			solarSystem;
	private World				world;
	private PhysicsFactory		phyFac;

	public boolean				debug						= false;
	public boolean				accelerate					= false;
	public boolean				deaccelerate				= false;
	public boolean				turnLeft					= false;
	public boolean				turnRight					= false;
	public boolean				shouldReset					= false;
	public boolean				showScore					= false;
	public boolean				shouldDestroyPlayer			= false;

	public float				fuel						= 500f;
	private float				fuelStart					= 500f;
	private float				fuelConsumptionTurn			= 0.0f;
	private float				fuelConsumptionAccelerate	= 1f;
	private float				acceleration				= 0.004f;
	private float				accelerationTurn			= 0.0000001f;
	private float				accDelta					= 0f;
	private int					planetCount					= 3;

	private BitmapFont			font;
	private ParticleEffect		sprayEffect;
	private ParticleEffect		jetEffect;

	private float				jetVolume					= 0f;
	private long				jetSoundId;
	private Sound				jetSound;

	public SolarSystemScreen(God god2) {
		this.god = god2;

		font = Assets.instance.skin.getFont("default-font");

		sprayEffect = new ParticleEffect();
		sprayEffect.load(Gdx.files.internal("particles/spray.fx"), Assets.instance.atlas);
		sprayEffect.scaleEffect(UNIT_SCALE);

		jetEffect = new ParticleEffect();
		jetEffect.load(Gdx.files.internal("particles/jet.fx"), Assets.instance.atlas);
		jetEffect.scaleEffect(UNIT_SCALE / 4f);

		jetSound = Assets.assetsManager.get(Assets.sndJet, Sound.class);
		jetSoundId = jetSound.loop(0f);
	}

	public void reset() {
		Gdx.app.log(TAG, "reseting world");
		if (solarSystem.getRank() == "C" || solarSystem.getRank() == "B" || solarSystem.getRank() == "A" || solarSystem.getRank() == "S") {
			planetCount++;
		}

		world.dispose();
		phyFac.dispose();
		sprayEffect.reset();

		show();
	}

	@Override
	public void show() {
		Gdx.app.log(TAG, "show SolorSystem");
		cam = new OrthographicCamera(Gdx.graphics.getWidth() * UNIT_SCALE, Gdx.graphics.getHeight() * UNIT_SCALE);
		cam.zoom = 0.5f;
		camPix = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.input.setInputProcessor(new SolarScreenInputProcessor(this));

		world = new World(new Vector2(0f, 0f), true);
		world.setContactListener(new SolarSystemContactListener(this));

		phyFac = new PhysicsFactory(world);

		createSolarSystem();

		fuel = fuelStart;
		accDelta = 0;
		showScore = false;
		shouldDestroyPlayer = false;
		sprayEffect.start();
	}

	private void createSolarSystem() {
		final float[] planets = new float[planetCount];
		for (int i = 0; i < planetCount; i++) {
			planets[i] = MathUtils.random(0.4f, 1.2f);
		}
		this.solarSystem = phyFac.createSolarSystem(planets);
		// for (int i = 0; i < 100; i++) {
		// update(TIME_STEP);
		// }

		player = phyFac.createPlayer();
		player.setTransform(0, 35, 0);
		player.setLinearVelocity(0f, 5f);
		player.setAngularDamping(1f);
	}

	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glClearColor(0, 0, 0, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

		if (debug) {
			god.batch.setColor(Color.WHITE);
			god.batch.setProjectionMatrix(cam.combined);
			god.batch.begin();

			god.box2dRenderer.render(world, cam.combined);

			god.batch.end();
		}
		{
			god.shapeRenderer.setProjectionMatrix(cam.combined);
			god.shapeRenderer.begin(ShapeType.Line);
			for (int i = solarSystem.getPlanets().size - 1; i >= 0; i--) {
				god.shapeRenderer.setColor(getColorForScore(solarSystem.getPlanetScores().get(i)));
				final Body planet = solarSystem.getPlanets().get(i);
				final Vector2 pos = planet.getWorldCenter();
				god.shapeRenderer.circle(0, 0, pos.len(), 128);
			}
			god.shapeRenderer.setColor(Color.WHITE);
			god.shapeRenderer.end();
		}
		if (player != null) {
			god.shapeRenderer.setProjectionMatrix(cam.combined);
			god.shapeRenderer.begin(ShapeType.Filled);
			// final Vector2 moveVec = player.getLinearVelocity().cpy().nor();
			final Vector2 moveVec = new Vector2(-1, 0).setAngle(player.getAngle() * MathUtils.radiansToDegrees + 180);
			final float x1 = player.getPosition().x + moveVec.x;
			final float y1 = player.getPosition().y + moveVec.y;
			moveVec.rotate(140);
			final float x2 = player.getPosition().x + moveVec.x;
			final float y2 = player.getPosition().y + moveVec.y;
			moveVec.rotate(80);
			final float x3 = player.getPosition().x + moveVec.x;
			final float y3 = player.getPosition().y + moveVec.y;
			god.shapeRenderer.triangle(x1, y1, x2, y2, x3, y3);
			god.shapeRenderer.end();
		}
		{
			god.batch.setColor(Color.WHITE);
			god.batch.setProjectionMatrix(cam.combined);
			god.batch.begin();
			Box2DSprite.draw(god.batch, world);
			god.batch.end();
		}
		if (player != null) {
			god.batch.setColor(Color.WHITE);
			god.batch.setProjectionMatrix(cam.combined);
			god.batch.begin();

			sprayEffect.draw(god.batch, delta);
			if (accelerate || deaccelerate) {
				jetEffect.start();
			} else {
				jetEffect.reset();
			}
			jetEffect.draw(god.batch, delta);

			god.batch.end();
		}
		{
			god.batch.setProjectionMatrix(camPix.combined);
			god.batch.begin();

			if (showScore) {
				font.setColor(Color.WHITE);

				final String result = player == null ? "Ship destroyed!\n" : "Out of fuel!\n";
				font.draw(god.batch,
						result + solarSystem.getScore() + " Life spread\n\nRank " + solarSystem.getRank() + "\n\nSpace to continue",
						-camPix.viewportWidth * 0.45f, camPix.viewportHeight / 4, camPix.viewportWidth * 0.9f, Align.center, false);
			} else {
				if (fuel < fuelStart / 10) {
					font.setColor(Color.RED);
				} else if (fuel < fuelStart / 3) {
					font.setColor(Color.ORANGE);
				} else {
					font.setColor(Color.WHITE);
				}
				font.draw(god.batch, "Fuel " + Math.round(fuel), -camPix.viewportWidth * 0.45f, -camPix.viewportHeight / 3,
						camPix.viewportWidth * 0.9f, Align.left, false);

				font.setColor(Color.WHITE);
				font.draw(god.batch, solarSystem.getScore() + " Life spread", -camPix.viewportWidth * 0.45f, -camPix.viewportHeight / 3,
						camPix.viewportWidth * 0.9f, Align.right, false);
			}

			god.batch.end();
		}
	}

	private Color getColorForScore(Integer score) {
		float green = MathUtils.clamp(0.5f, 1f, score / 1000f);
		if (score == 0) {
			green = 0.5f;
		}
		final Color color = new Color(1f - green, green, 1f - green, 0.75f);
		return color;
	}

	private void update(float delta) {
		if (shouldReset) {
			shouldReset = false;
			reset();
		} else if (shouldDestroyPlayer && player != null) {
			Box2DUtils.destroyFixtures(player);
			world.destroyBody(player);
			player = null;
		}

		accDelta += delta;
		solarSystem.update(player);

		if (player != null && fuel > 0f) {
			if (accelerate || deaccelerate) {
				jetVolume = 1f;
			} else {
				jetVolume = 0f;
			}
			if (accelerate) {
				// final Vector2 accVec = player.getLinearVelocity().cpy().nor().scl(player.getMass());
				final Vector2 accVec = new Vector2(1, 0).setAngle(player.getAngle() * MathUtils.radiansToDegrees).scl(-acceleration);
				player.applyForceToCenter(accVec, true);
				fuel -= fuelConsumptionAccelerate;
			}
			if (deaccelerate) {
				// final Vector2 accVec = player.getLinearVelocity().cpy().nor().scl(-player.getMass());
				// Gdx.app.log(TAG, "player.getMass() * 50 " + player.getMass() * 50);
				final Vector2 accVec = new Vector2(1, 0).setAngle(player.getAngle() * MathUtils.radiansToDegrees).scl(acceleration);
				player.applyForceToCenter(accVec, true);
				fuel -= fuelConsumptionAccelerate;
			}
			if (turnLeft) {
				player.applyTorque(accelerationTurn, true);
				fuel -= fuelConsumptionTurn;
			}
			if (turnRight) {
				player.applyTorque(-accelerationTurn, true);
				fuel -= fuelConsumptionTurn;
			}
		} else {
			jetVolume = 0f;
		}
		if (fuel < 0f) {
			fuel = 0;
		}
		if (fuel == 0f) {
			showScore = true;
		}

		world.step(TIME_STEP, VELOCITY_ITERS, POSITION_ITERS);

		if (player != null) {
			sprayEffect.setPosition(player.getWorldCenter().x, player.getWorldCenter().y);
			jetEffect.setPosition(player.getWorldCenter().x, player.getWorldCenter().y);
			final ParticleEmitter emitter = jetEffect.getEmitters().first();
			final float deaccelRot = deaccelerate ? 180 : 0;
			emitter.getAngle().setHigh(MathUtils.radiansToDegrees * player.getAngle() + deaccelRot);
			emitter.getAngle().setLow(MathUtils.radiansToDegrees * player.getAngle() + deaccelRot);

			final Vector2 camCenter = player.getWorldCenter().cpy().scl(0.75f);
			cam.position.x = camCenter.x;
			cam.position.y = camCenter.y;
			cam.update();
		}

		jetSound.setVolume(jetSoundId, jetVolume);
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportWidth = width * UNIT_SCALE;
		cam.viewportHeight = height * UNIT_SCALE;
		cam.update();

		camPix.viewportWidth = width;
		camPix.viewportHeight = height;
		camPix.update();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
		world.dispose();
		phyFac.dispose();
		sprayEffect.dispose();
	}

}
