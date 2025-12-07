/*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jants.ui;

import com.sun.j3d.audioengines.javasound.JavaSoundMixer;
import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.universe.ViewerAvatar;
import org.jants.log.StatisticsCollector;
import org.apache.logging.log4j.Level;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

// view of the map, including the nest
// simulation controls
// statistics gathered though logging
public class ColonyVisualizer3D extends JFrame implements ActionListener {

    private ResourceBundle resourceBundle;

    private Map userProperties;

    private Canvas3D canvas3D;
    private JavaSoundMixer javaSoundMixer;

    private float fieldOfView = (float) Math.PI / 2;
    private float frontClip = 0.01f;
    private float backClip = 30.0f;

    private VirtualUniverse virtualUniverse;
    private Locale locale;
    private PhysicalBody physicalBody;
    private PhysicalEnvironment physicalEnvironment;
    boolean volumeMenuEnabled;
    private View view;

    private BranchGroup WorldBranchGroup;
    private BranchGroup ObjectsBranchGroup;
    private BranchGroup ToolsBranchGroup;
    private BranchGroup LightsBranchGroup;
    private BranchGroup BrowserBranchGroup;
    private ViewerAvatar AvatarBranchGroup;
    private PlatformGeometry ConsoleBranchGroup;
    private BranchGroup UserBranchGroup;
    private TransformGroup UserTransform;

    private ViewPlatform viewPlatform;
    private float activationRadius;

    private BoundingSphere browserBounds;
    private BoundingSphere lightBounds;

    private WalkViewerBehavior2 walkViewerBehavior2;
    private ExamineViewerBehavior examineViewerBehavior;
    private FlyViewerBehavior flyViewerBehavior;

    private StandardPickViewer standardPickViewer;

    private static final int cursorPreferredWidth = 24;
    private static final int cursorPreferredHeight = 24;

    private final static float highlightLevel = 0.2f;

    private DirectionalLight headLight;
    private AmbientLight ambientLight;

    private static final int WALK_MODE = 0;
    private static final int EXAMINE_MODE = 1;
    private static final int FLY_MODE = 2;

    private Switch TheSwitch;

    private Cursor walkCursor;
    private Cursor examineCursor;
    private Cursor flyCursor;

    private Cursor pickCursor;

    private KeyNavigatorBehavior keyNavigatorBehavior;

    public ColonyVizualizer3D() {

        //3D initilization
        virtualUniverse = new VirtualUniverse();
        locale = new Locale(virtualUniverse);
        physicalBody = new PhysicalBody();
        physicalEnvironment = new PhysicalEnvironment();
        javaSoundMixer = new JavaSoundMixer(physicalEnvironment);
        volumeMenuEnabled = javaSoundMixer.initialize();
        fieldOfView = (float) Math.PI / 2;
        frontClip = 0.01f;
        backClip = 30.0f;
        view = new View();
        view.setFieldOfView(fieldOfView);
        view.setFrontClipDistance(frontClip);
        view.setBackClipDistance(backClip);
        view.addCanvas3D(canvas3D);
        view.setPhysicalBody(physicalBody);
        view.setPhysicalEnvironment(physicalEnvironment);

        ObjectsBranchGroup = new BranchGroup();
        ObjectsBranchGroup.setCapability(Node.ALLOW_BOUNDS_READ);
        ObjectsBranchGroup.setCapability(Group.ALLOW_CHILDREN_READ);
        ObjectsBranchGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
        ObjectsBranchGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        UserBranchGroup = new BranchGroup();
        UserBranchGroup.setCapability(Node.ALLOW_BOUNDS_READ);
        UserBranchGroup.setCapability(Group.ALLOW_CHILDREN_READ);
        UserBranchGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
        UserBranchGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        UserTransform = new TransformGroup();
        UserTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        UserTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        UserTransform.setCapability(Group.ALLOW_CHILDREN_READ);
        UserTransform.setCapability(Group.ALLOW_CHILDREN_WRITE);
        UserTransform.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        UserBranchGroup.addChild(UserTransform);

        viewPlatform = new ViewPlatform();
        viewPlatform.setActivationRadius(activationRadius);

        view.attachViewPlatform(viewPlatform);
        UserTransform.addChild(viewPlatform);

        LightsBranchGroup = new BranchGroup();
        LightsBranchGroup.setCapability(Node.ALLOW_BOUNDS_READ);
        LightsBranchGroup.setCapability(Group.ALLOW_CHILDREN_READ);
        LightsBranchGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
        LightsBranchGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        UserTransform.addChild(LightsBranchGroup);

        lightBounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.MAX_VALUE);

        headLight = new DirectionalLight(((Boolean) userProperties.get("userheadlight")).booleanValue(), new Color3f(0.8f, 0.8f, 0.8f), new Vector3f(0.0f, 0.0f, -1.0f));
        headLight.setCapability(Light.ALLOW_STATE_WRITE);
        headLight.setInfluencingBounds(lightBounds);

        LightsBranchGroup.addChild(headLight);

        ambientLight = new AmbientLight(((Boolean) userProperties.get("userambientlight")).booleanValue(), new Color3f(0.2f, 0.2f, 0.2f));
        ambientLight.setInfluencingBounds(lightBounds);
        ambientLight.setCapability(Light.ALLOW_STATE_WRITE);

        LightsBranchGroup.addChild(ambientLight);

        BrowserBranchGroup = new BranchGroup();
        BrowserBranchGroup.setCapability(Group.ALLOW_CHILDREN_READ);
        BrowserBranchGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);

        Image image;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension cursorDimension = toolkit.getBestCursorSize(cursorPreferredWidth, cursorPreferredHeight);
        int maximumcolors = toolkit.getMaximumCursorColors();

        if ((cursorDimension.getHeight() != 0) && (cursorDimension.getWidth() != 0) && (maximumcolors > 0)) {
            //XXXX should make more tests
            image = toolkit.getImage("images/walkcursor.gif");
            try {
                walkCursor = toolkit.createCustomCursor(image, new Point(8, 8), resourceBundle.getString("WalkCursorDescription"));
            } catch (IndexOutOfBoundsException exception) {
                walkCursor = new Cursor(Cursor.MOVE_CURSOR);
            }
            image.flush();
            image = toolkit.getImage("images/examinecursor.gif");
            try {
                examineCursor = toolkit.createCustomCursor(image, new Point(8, 8), resourceBundle.getString("ExamineCursorDescription"));
            } catch (IndexOutOfBoundsException exception) {
                examineCursor = new Cursor(Cursor.MOVE_CURSOR);
            }
            image.flush();
            image = toolkit.getImage("images/flycursor.gif");
            try {
                flyCursor = toolkit.createCustomCursor(image, new Point(8, 8), resourceBundle.getString("FlyCursorDescription"));
            } catch (IndexOutOfBoundsException exception) {
                flyCursor = new Cursor(Cursor.MOVE_CURSOR);
            }
            image.flush();
        } else {
            walkCursor = new Cursor(Cursor.MOVE_CURSOR);
            examineCursor = new Cursor(Cursor.MOVE_CURSOR);
            flyCursor = new Cursor(Cursor.MOVE_CURSOR);
            pickCursor = new Cursor(Cursor.HAND_CURSOR);
        }

        browserBounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.MAX_VALUE);

        walkViewerBehavior2 = new WalkViewerBehavior2(UserTransform, canvas3D);
        walkViewerBehavior2.setSchedulingBounds(browserBounds);
        walkViewerBehavior2.setActiveCursor(walkCursor);
        examineViewerBehavior = new ExamineViewerBehavior(UserTransform, canvas3D);
        examineViewerBehavior.setSchedulingBounds(browserBounds);
        examineViewerBehavior.setActiveCursor(examineCursor);
        flyViewerBehavior = new FlyViewerBehavior(UserTransform, canvas3D);
        flyViewerBehavior.setSchedulingBounds(browserBounds);
        flyViewerBehavior.setActiveCursor(flyCursor);

        TheSwitch = new Switch();
        TheSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);

        //order is important here WALK_MODE = 0; EXAMINE_MODE = 1; FLY_MODE = 2;
        TheSwitch.addChild(walkViewerBehavior2);
        TheSwitch.addChild(examineViewerBehavior);
        TheSwitch.addChild(flyViewerBehavior);

        BrowserBranchGroup.addChild(TheSwitch);

        if ((cursorDimension.getHeight() != 0) && (cursorDimension.getWidth() != 0) && (maximumcolors > 0)) {
            image = toolkit.getImage("images/pickcursor.gif");
            try {
                pickCursor = toolkit.createCustomCursor(image, new Point(8, 8), resourceBundle.getString("PickCursorDescription"));
            } catch (IndexOutOfBoundsException exception) {
                pickCursor = new Cursor(Cursor.HAND_CURSOR);
            }
            image.flush();
        } else {
            pickCursor = new Cursor(Cursor.HAND_CURSOR);
        }
        standardPickViewer = new StandardPickViewer(ObjectsBranchGroup, canvas3D, browserBounds);
        standardPickViewer.setHighlightLevel(highlightLevel);
        standardPickViewer.setActiveCursor(pickCursor);
        BrowserBranchGroup.addChild(standardPickViewer);

        keyNavigatorBehavior = new KeyNavigatorBehavior(UserTransform);

        BrowserBranchGroup.addChild(keyNavigatorBehavior);

        TheSwitch.setWhichChild(((Integer) userProperties.get("usernavigationmode")).intValue());

        UserTransform.addChild(BrowserBranchGroup);

        AvatarBranchGroup = new ViewerAvatar();
        //Avatar
        UserTransform.addChild(AvatarBranchGroup);

        ConsoleBranchGroup = new PlatformGeometry();
        //Console
        UserTransform.addChild(ConsoleBranchGroup);

        LightsBranchGroup.compile();
        BrowserBranchGroup.compile();
        AvatarBranchGroup.compile();
        ConsoleBranchGroup.compile();

        UserBranchGroup.compile();
        highResolutionLocale.addBranchGraph(UserBranchGroup);
    }

    // adapted after https://community.oracle.com/tech/developers/discussion/1275759/implementing-mesh-object-in-java-3d
    public BranchGroup getMesh3DBranchGroup(Mesh3D mesh) {

        if (mesh.nodes.size() == 0 || mesh.faces.size() == 0)
            return null;
        Point3f[] nodes = new Point3f[mesh.nodes.size()];
        mesh.nodes.toArray(nodes);
        int[] indices = mesh.getFaceIndexArray();

        //show fill if selected
        if (((arBoolean)attributes.getValue("HasFill")).value) {
            //generate normals for shading
            GeometryInfo gi = new  GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
            gi.setCoordinates(nodes);
            gi.setCoordinateIndices(indices);
            NormalGenerator ng = new NormalGenerator();
            ng.generateNormals(gi);

            IndexedTriangleArray triArray = (IndexedTriangleArray)gi.getIndexedGeometryArray();
            //branch group respresenting this shape
            BranchGroup thisNode = new BranchGroup();

            //add geometry to shape node
            javax.media.j3d.Shape3D fillShapeNode = new javax.media.j3d.Shape3D(triArray);

            //set fill colour
            Appearance fillAppNode = new Appearance();
            Color thisColour = (Color)attributes.getValue("FillColour");

            //turn off back culling
            PolygonAttributes pAtt = new PolygonAttributes();
            pAtt.setCullFace(PolygonAttributes.CULL_NONE);
            pAtt.setBackFaceNormalFlip(true);
            fillAppNode.setPolygonAttributes(pAtt);
            Material m = new Material();
            m.setDiffuseColor(new Color3f(thisColour));
            //TransparencyAttributes ta = new TransparencyAttributes();
            //ta.setTransparency(0.5f);
            //ta.setTransparencyMode(TransparencyAttributes.NICEST);
            fillAppNode.setMaterial(m);

            //apply appearance settings
            fillShapeNode.setAppearance(fillAppNode);
            //fillAppNode.setTransparencyAttributes(ta);

            thisNode.addChild(fillShapeNode);
        }

        //show edges if selected
        if (((arBoolean)attributes.getValue("ShowEdges")).value) {
            IndexedTriangleArray edgeArray = new  IndexedTriangleArray(nodes.length,
                    IndexedTriangleArray.COORDINATES,
                    indices.length);

            //set geometry
            edgeArray.setCoordinates(0, nodes);
            edgeArray.setCoordinateIndices(0, indices);
            javax.media.j3d.Shape3D edgeShapeNode = new javax.media.j3d.Shape3D(edgeArray);
            Appearance edgeAppNode = new Appearance();
            Color edgeColour = (Color)attributes.getValue("LineColour");
            ColoringAttributes cAtt = new ColoringAttributes();
            PolygonAttributes pAtt = new PolygonAttributes();
            pAtt.setPolygonMode(PolygonAttributes.POLYGON_LINE);
            pAtt.setCullFace(PolygonAttributes.CULL_NONE);
            pAtt.setBackFaceNormalFlip(true);
            cAtt.setColor(new Color3f(edgeColour));
            edgeAppNode.setColoringAttributes(cAtt);
            edgeAppNode.setPolygonAttributes(pAtt);
            edgeAppNode.setMaterial(new Material());
            edgeShapeNode.setAppearance(edgeAppNode);
            thisNode.addChild(edgeShapeNode);
        }

        return thisNode;
    }

    public void actionPerformed(ActionEvent evt) {
        //should allow to manipulate the view, control the simulation, and change events in realtime
    }

    public void main(String[] args) {
        StatisticsCollector.init();
        StatisticsCollector.logAtLevel(Level.INFO, "Starting JAnts application.");
        ColonyVisualizer3D colonyVisualizer3D;
        colonyVisualizer3D = new ColonyVisualizer3D();
    }

}
