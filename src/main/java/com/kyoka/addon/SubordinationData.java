package com.kyoka.addon;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SubordinationData extends WorldSavedData {
    public static final String NAME = "kyoka_subordinates";
    private final Map<UUID, Set<UUID>> map = new HashMap<>();

    public SubordinationData() {
        super(NAME);
    }

    public Set<UUID> of(UUID owner) {
        return map.computeIfAbsent(owner, k -> new HashSet<>());
    }

    public boolean contains(UUID owner, UUID entity) {
        Set<UUID> set = map.get(owner);
        return set != null && set.contains(entity);
    }

    public void removeEverywhere(UUID entity) {
        boolean changed = false;
        for (Set<UUID> set : map.values()) {
            changed |= set.remove(entity);
        }
        if (changed) setDirty();
    }

    @Override
    public void load(CompoundNBT nbt) {
        map.clear();
        ListNBT owners = nbt.getList("owners", 10);
        for (int i = 0; i < owners.size(); i++) {
            CompoundNBT entry = owners.getCompound(i);
            UUID owner = UUID.fromString(entry.getString("owner"));
            Set<UUID> set = new HashSet<>();
            ListNBT subs = entry.getList("subs", 8);
            for (int j = 0; j < subs.size(); j++) {
                set.add(UUID.fromString(subs.getString(j)));
            }
            map.put(owner, set);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        ListNBT owners = new ListNBT();
        for (Map.Entry<UUID, Set<UUID>> e : map.entrySet()) {
            CompoundNBT entry = new CompoundNBT();
            entry.putString("owner", e.getKey().toString());
            ListNBT subs = new ListNBT();
            for (UUID u : e.getValue()) {
                subs.add(StringNBT.valueOf(u.toString()));
            }
            entry.put("subs", subs);
            owners.add(entry);
        }
        nbt.put("owners", owners);
        return nbt;
    }
}
