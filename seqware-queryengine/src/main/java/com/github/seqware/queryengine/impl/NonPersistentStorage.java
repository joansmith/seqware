/*
 * Copyright (C) 2012 SeqWare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.seqware.queryengine.impl;

import com.github.seqware.queryengine.model.Atom;
import com.github.seqware.queryengine.model.Feature;
import com.github.seqware.queryengine.model.impl.AtomImpl;
import com.github.seqware.queryengine.model.impl.FeatureList;
import com.github.seqware.queryengine.util.SGID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Doesn't really store things anywhere, just keeps it in memory. This is
 * totally inefficient and is meant only for testing and prototyping purposes
 *
 * @author dyuen
 */
public class NonPersistentStorage extends StorageInterface {

    private Map<String, ByteTypePair> map = new HashMap<String, ByteTypePair>();
    private final SerializationInterface serializer;

    public NonPersistentStorage(SerializationInterface i) {
        this.serializer = i;
    }

    @Override
    public void serializeAtomToTarget(Atom obj) {
        AtomImpl objImpl = (AtomImpl) obj;
        Class cl = objImpl.getHBaseClass();
        if (objImpl.getPrecedingSGID() != null) {
            assert (!(objImpl.getPrecedingSGID().equals(objImpl.getSGID())));
        }
//        obj.getSGID().setBackendTimestamp(new Date(System.currentTimeMillis()));
        ByteTypePair pair = new ByteTypePair(serializer.serialize(obj), cl);
        map.put(obj.getSGID().getRowKey().toString() + separator + obj.getSGID().getBackendTimestamp().getTime(), pair);
    }

    @Override
    public <T extends Atom> void serializeAtomsToTarget(T... atomArr) {
        for (Atom obj : atomArr) {
            serializeAtomToTarget(obj);
        }
    }

    @Override
    public Atom deserializeTargetToAtom(SGID sgid) {
        if (!map.containsKey(sgid.getRowKey().toString() + separator + sgid.getBackendTimestamp().getTime())) {
            return null;
        }
        Atom a = (Atom) serializer.deserialize(map.get(sgid.getRowKey().toString() + separator + sgid.getBackendTimestamp().getTime()).bArr, map.get(sgid.getRowKey().toString() + separator + sgid.getBackendTimestamp().getTime()).cl);
        if (a instanceof FeatureList){
            for(Feature f : ((FeatureList)a).getFeatures()){
                if (f.getSGID().equals(sgid)){
                    return f;
                }
            }
        }
        return a;
    }

    @Override
    public void clearStorage() {
        map.clear();
    }

    @Override
    public Iterable<SGID> getAllAtoms() {
        List<SGID> list = new ArrayList<SGID>();
        for (Entry<String, ByteTypePair> e : map.entrySet()) {
            Atom a = (Atom) serializer.deserialize(e.getValue().bArr, e.getValue().cl);
            list.add(a.getSGID());
        }
        return list;
    }

    @Override
    public <T extends Atom> T deserializeTargetToAtom(Class<T> t, SGID sgid) {
        // this storage type will need to look for buckets that are appropriate
        if (t == Feature.class) {
            for (SGID id : getAllAtoms()) {
                String rowKey1 = id.getRowKey().toString() + separator + id.getBackendTimestamp().getTime();
                String rowKey2 = sgid.getRowKey().toString() + separator + sgid.getBackendTimestamp().getTime();
                if (rowKey1.equals(rowKey2)) {
                    FeatureList fList = (FeatureList) this.deserializeTargetToAtom(id);
                    for (Feature f : fList.getFeatures()) {
                        if (sgid.equals(f.getSGID())) {
                            return (T) f;
                        }
                    }
                }
            }
        }
        return (T) this.deserializeTargetToAtom(sgid);
    }

    @Override
    public <T extends Atom> List<T> deserializeTargetToAtoms(Class<T> t, SGID... sgid) {
        List<T> list = new ArrayList<T>();
        for (SGID sg : sgid) {
            list.add((T) this.deserializeTargetToAtom(sg));
        }
        return list;
    }

    @Override
    public Atom deserializeTargetToLatestAtom(SGID sgid) {
        List<Atom> aList = new ArrayList<Atom>();
        String rowKey = sgid.getRowKey().toString() + separator + sgid.getBackendTimestamp().getTime();
        for (Entry<String, ByteTypePair> e : map.entrySet()) {
            if (e.getKey().equals(rowKey)) {
                aList.add((Atom) serializer.deserialize(e.getValue().bArr, e.getValue().cl));
            }
        }
        // check for latest one, HBase will do this much more efficiently
        if (aList.isEmpty()) {
            return null;
        }
        Atom latest = aList.get(0);
        for (Atom a : aList) {
            if (a.getTimestamp().after(latest.getTimestamp())) {
                latest = a;
            }
        }
        return latest;
    }

    @Override
    public <T extends Atom> T deserializeTargetToLatestAtom(SGID sgid, Class<T> t) {
        return (T) this.deserializeTargetToLatestAtom(sgid);
    }

    @Override
    public void closeStorage() {
        /**
         * ignore this, we don't need to do anything in particular
         */
    }

    public class ByteTypePair {

        private byte[] bArr;
        private Class cl;

        public ByteTypePair(byte[] bArr, Class cl) {
            this.bArr = bArr;
            this.cl = cl;
        }
    }
}
