package headmade.god;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

public class God extends Game {
	private static final String	TAG	= God.class.getName();

	OrthographicCamera			cam;
	SpriteBatch					batch;
	ShapeRenderer				shapeRenderer;
	Box2DDebugRenderer			box2dRenderer;

	@Override
	public void create() {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		box2dRenderer = new Box2DDebugRenderer();

		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		Assets.instance.init();
		Assets.instance.loadAll();
		Assets.assetsManager.finishLoading();
		Assets.instance.onFinishLoading();

		setScreen(new SolarSystemScreen(this));
	}

	@Override
	public void render() {

		final float deltaTime = Math.min(Gdx.graphics.getDeltaTime(), 1.0f / 60.0f);
		screen.render(deltaTime);

	}

	@Override
	public void dispose() {
		super.dispose();
		batch.dispose();
		shapeRenderer.dispose();
		box2dRenderer.dispose();
		Assets.instance.dispose();
	}
}
