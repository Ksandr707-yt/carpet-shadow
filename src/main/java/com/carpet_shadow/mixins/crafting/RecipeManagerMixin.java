package com.carpet_shadow.mixins.crafting;

import com.carpet_shadow.CarpetShadow;
import com.carpet_shadow.CarpetShadowSettings;
import com.carpet_shadow.Globals;
import com.carpet_shadow.interfaces.ShadowItem;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked","rawtypes"})
@Mixin(ServerRecipeManager.class)
public class RecipeManagerMixin {
    @Inject(method = "apply(Lnet/minecraft/recipe/PreparedRecipes;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;", shift = At.Shift.BY, by=2))
    private void addShadowRecipe(PreparedRecipes preparedRecipes, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci, @Local(ordinal = 0) ImmutableMultimap.Builder<RecipeType<?>, RecipeEntry<?>> builder, @Local(ordinal = 0) ImmutableMap.Builder<Identifier, RecipeEntry<?>> builder2){
        Identifier identifier = Identifier.of("carpet_shadow","shadow_recipe");
        Recipe<?> recipe = new BookCloningRecipe(CraftingRecipeCategory.MISC) {
            @Override
            public boolean matches(CraftingRecipeInput inventory, World world) {
                if (CarpetShadowSettings.shadowItemMode== CarpetShadowSettings.Mode.UNLINK || !CarpetShadowSettings.shadowCraftingGeneration)
                    return false;
                boolean enderchest = false;
                List<ItemStack> stacks = new ArrayList<>();
                int count = 0;
                for(int i = 0; i < inventory.size(); ++i) {
                    ItemStack itemStack2 = inventory.getStackInSlot(i);
                    if (!itemStack2.isEmpty()) {
                        if (itemStack2.getItem().equals(Items.ENDER_CHEST) && !enderchest)
                            enderchest = true;
                        else {
                            stacks.add(itemStack2);
                        }
                        count++;
                    }
                }
                var matches = enderchest && count == 2;
//                if (!matches) for (var it : stacks) {
//                    var shadowId = ((ShadowItem) (Object) it).carpet_shadow$getShadowId();
//                    if ((shadowId != null && !shadowId.isEmpty() && shadowId.matches("\\S+?")) && !Globals.isShadowIdExists(shadowId))
//                        ((ShadowItem) (Object) it).carpet_shadow$removeShadow();
//                }
                return matches;
            }

            @Override
            public ItemStack craft(CraftingRecipeInput inventory, RegistryWrapper.WrapperLookup wrapperLookup) {
                if (CarpetShadowSettings.shadowItemMode== CarpetShadowSettings.Mode.UNLINK || !CarpetShadowSettings.shadowCraftingGeneration)
                    return ItemStack.EMPTY;

                ItemStack item = null;
                ItemStack enderchest = null;
                for(int i = 0; i < inventory.size(); ++i) {
                    ItemStack itemStack2 = inventory.getStackInSlot(i);
                    if (!itemStack2.isEmpty()) {
                        if (itemStack2.getItem().equals(Items.ENDER_CHEST)) {
                            if (enderchest != null)
                                item = enderchest;
                            enderchest = itemStack2;
                        }else
                            item = itemStack2;
                    }
                }
                if (item==null || enderchest==null)
                    return ItemStack.EMPTY;

                String id = ((ShadowItem)(Object)item).carpet_shadow$getShadowId();
                if (id == null){
                    id = CarpetShadow.shadow_id_generator.nextString();
                }
                return Globals.getByIdOrAdd(id, item);
            }

            @Override
            public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput inventory) {
                ItemStack item = null;
                ItemStack enderchest = null;
                for(int i = 0; i < inventory.size(); ++i) {
                    ItemStack itemStack2 = inventory.getStackInSlot(i);
                    if (!itemStack2.isEmpty()) {
                        if (itemStack2.getItem().equals(Items.ENDER_CHEST)) {
                            if (enderchest != null)
                                item = enderchest;
                            enderchest = itemStack2;
                        }else
                            item = itemStack2;
                    }
                }

                if (item != null && enderchest != null)
                    item.setCount(item.getCount() + 1);

                return super.getRecipeRemainders(inventory);
            }

            public boolean fits(int width, int height) {
                if (CarpetShadowSettings.shadowItemMode== CarpetShadowSettings.Mode.UNLINK || !CarpetShadowSettings.shadowCraftingGeneration)
                    return false;
                return width * height >= 2;
            }
        };
        RecipeEntry<?> recipeEntry = new RecipeEntry(RegistryKey.ofRegistry(identifier), recipe);
        builder.put(recipe.getType(), recipeEntry);
        builder2.put(identifier, recipeEntry);
    }
}