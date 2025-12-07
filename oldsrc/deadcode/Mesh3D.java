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

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

/*******************
 * Geometry class to represent a 3D triangular mesh. This is implemented as an array
 * of nodes and an array of MeshFace3D objects, which hold the indices of the three nodes
 * of the triangular face.
 *
 * From https://community.oracle.com/tech/developers/discussion/1275759/implementing-mesh-object-in-java-3d
 *
 * @author Andrew Reid
 * @version 1.0
 */

public class Mesh3D extends Shape3D {

    public ArrayList<Point3f> nodes = new ArrayList<Point3f>();
    public ArrayList<MeshFace3D> faces = new ArrayList<MeshFace3D>();

    public Mesh3D(){

    }

    public int addNode(Point3f n){
        nodes.add(n);
        return nodes.size() - 1;
    }

    public void addNode(Point3f n, int i){
        nodes.add(i, n);
    }

    public boolean addFace(int a, int b, int c){
        if (a >= nodes.size() || b >= nodes.size() || c >= nodes.size())
            return false;
        return faces.add(new MeshFace3D(a, b, c));
    }

    public void removeNode(int i){
        //complication is we need to find and remove all faces with i as nodes
        //plus decrement all indices >= i
        nodes.remove(i);
        for (int a = 0; a < faces.size(); a++){
            if (faces.get(a).hasNode(i)){
                faces.remove(a);
                a--;
            }else{
                if (faces.get(a).A > i) faces.get(a).A--;
                if (faces.get(a).B > i) faces.get(a).B--;
                if (faces.get(a).C > i) faces.get(a).C--;
            }
        }
    }

    public void removeFace(int i){
        faces.remove(i);
    }

    public ArrayList<Point3f> getNodes(){
        ArrayList<Point3f> retNodes = new ArrayList<Point3f>(nodes);
        return retNodes;
    }

    public void setNodes(ArrayList<Point3f> n){
        nodes = n;
    }

    public int[] getFaceIndexArray(){
        int[] retArray = new int[faces.size() * 3];
        for (int i = 0; i < faces.size(); i++){
            retArray[i * 3] = faces.get(i).A;
            retArray[i * 3 + 1] = faces.get(i).B;
            retArray[i * 3 + 2] = faces.get(i).C;
        }
        return retArray;
    }

    public void finalize(){
        nodes.trimToSize();
        faces.trimToSize();
    }

    //is this face clockwise?
    public boolean isClockwiseFace(int i){
        return GeometryFunctions.isClockwise(nodes.get(faces.get(i).A),
                nodes.get(faces.get(i).B),
                nodes.get(faces.get(i).C));
    }

    //inner class to define a triangular mesh face
    public class MeshFace3D{
        //indices of this face's nodes, in clockwise order of A, B, C
        public int A, B, C;

        public MeshFace3D(int a, int b, int c){
            A = a;
            B = b;
            C = c;
        }

        public MeshFace3D(MeshFace3D f){
            A = f.A;
            B = f.B;
            C = f.C;
        }

        public boolean hasNode(int i){
            return A == i || B == i || C == i;
        }
    }
}