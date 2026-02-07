package de.geolykt.s2dmenues.asm;

import java.net.URI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.slf4j.LoggerFactory;
import org.stianloader.sll.transform.CodeTransformer;

import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;

import de.geolykt.starloader.transformers.ASMTransformer;

public class TextraMASMTransformer extends ASMTransformer implements CodeTransformer {

    private static final String O2L_LONG_MAP = "com/badlogic/gdx/utils/ObjectLongMap";
    private static final String JU_MAP = "java/util/Map";
    private static final String JU_HASHMAP = "java/util/HashMap";

    @Override
    public boolean accept(@NotNull ClassNode source) {
        if (MinestomRootClassLoader.getInstance().isThreadLoggingClassloadingFailures()) {
            try {
                throw new RuntimeException("Stacktrace");
            } catch (RuntimeException e) {
                LoggerFactory.getLogger(TextraMASMTransformer.class).warn("TextraMASMTransformer implements CodeTransformer, meaning that CodeTransformer#transformClass should be called instead of ASMTransformer#accept. Please report this issue to the caller. Note: This is not a fatal issue, but should be handled in due time.", e);
            }
        }

        return this.transformClass(source, null);
    }

    @Override
    public boolean isValidTarget(@NotNull String internalName) {
        if (MinestomRootClassLoader.getInstance().isThreadLoggingClassloadingFailures()) {
            try {
                throw new RuntimeException("Stacktrace");
            } catch (RuntimeException e) {
                LoggerFactory.getLogger(TextraMASMTransformer.class).warn("TextraMASMTransformer implements CodeTransformer, meaning that CodeTransformer#isValidTarget(String, URI) should be called instead of ASMTransformer#isValidTarget(String). Please report this issue to the caller. Note: This is not a fatal issue, but should be handled in due time.", e);
            }
        }

        return this.isValidTarget(internalName, null);
    }

    @Override
    public boolean isValidTarget(@NotNull String internalName, @Nullable URI codeSourceURI) {
        return internalName.startsWith("com/github/tommyettinger/textra/");
    }

    @Override
    public boolean transformClass(@NotNull ClassNode node, @Nullable URI codeSourceURI) {
        boolean transformed = false;

        for (FieldNode field : node.fields) {
            if (field.desc.equals("L" + TextraMASMTransformer.O2L_LONG_MAP + ";")) {
                field.signature = "L" + TextraMASMTransformer.JU_MAP + field.signature.substring(field.desc.length() - 1, field.signature.length() - 2) + "Ljava/lang/Long;>;";
                field.desc = "L" + TextraMASMTransformer.JU_MAP + ";";
                transformed = true;
            }
        }

        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                switch (insn.getOpcode()) {
                case Opcodes.GETFIELD:
                case Opcodes.PUTFIELD:
                    if (((FieldInsnNode) insn).desc.equals("L" + TextraMASMTransformer.O2L_LONG_MAP + ";")) {
                        ((FieldInsnNode) insn).desc = "L" + TextraMASMTransformer.JU_MAP + ";";
                        transformed = true;
                    }
                    break;
                case Opcodes.INVOKEVIRTUAL: {
                    MethodInsnNode minsn = (MethodInsnNode) insn;
                    if (minsn.owner.equals(TextraMASMTransformer.O2L_LONG_MAP)) {
                        if (minsn.name.equals("putAll")) {
                            if (!minsn.desc.equals("(L" + TextraMASMTransformer.O2L_LONG_MAP + ";)V")) {
                                throw new AssertionError("Unexpected descriptor of putAll instruction: " + minsn.desc);
                            }
                            minsn.desc = "(L" + TextraMASMTransformer.JU_MAP + ";)V";
                        } else if (minsn.name.equals("clear")) {
                            if (!minsn.desc.equals("()V")) {
                                throw new AssertionError("Unexpected descriptor of clear instruction: " + minsn.desc);
                            }
                        } else if (minsn.name.equals("put")) {
                            if (!minsn.desc.equals("(Ljava/lang/Object;J)V")) {
                                throw new AssertionError("Unexpected descriptor of put instruction: " + minsn.desc);
                            }
                            minsn.desc = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
                            method.instructions.insertBefore(minsn, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;"));
                            method.instructions.insert(minsn, new InsnNode(Opcodes.POP));
                        } else if (minsn.name.equals("remove")) {
                            if (!minsn.desc.equals("(Ljava/lang/Object;J)J")) {
                                throw new AssertionError("Unexpected descriptor of remove instruction: " + minsn.desc);
                            }
                            minsn.desc = "(Ljava/lang/Object;)Ljava/lang/Object;";
                            // Why must this infernal code tease me so
                            method.instructions.insertBefore(minsn, new InsnNode(Opcodes.DUP2_X2)); // Note: swap does not work for category 2
                            method.instructions.insertBefore(minsn, new InsnNode(Opcodes.POP2));

                            InsnList insertInstructions = new InsnList();
                            insertInstructions.add(new InsnNode(Opcodes.DUP));
                            LabelNode labelNonnull = new LabelNode();
                            insertInstructions.add(new JumpInsnNode(Opcodes.IFNONNULL, labelNonnull));
                            // long, !null
                            insertInstructions.add(new InsnNode(Opcodes.POP));
                            // long
                            LabelNode labelEnd = new LabelNode();
                            insertInstructions.add(new JumpInsnNode(Opcodes.GOTO, labelEnd));
                            insertInstructions.add(labelNonnull);
                            // long, !null
                            insertInstructions.add(new InsnNode(Opcodes.DUP_X2));
                            insertInstructions.add(new InsnNode(Opcodes.POP));
                            // !null, long
                            insertInstructions.add(new InsnNode(Opcodes.POP2));
                            // !null
                            insertInstructions.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Long"));
                            // !null = java/lang/Long
                            insertInstructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J"));
                            // long
                            insertInstructions.add(labelEnd);

                            method.instructions.insert(minsn, insertInstructions);
                        } else if (minsn.name.equals("get")) {
                            if (!minsn.desc.equals("(Ljava/lang/Object;J)J")) {
                                throw new AssertionError("Unexpected descriptor of get instruction: " + minsn.desc);
                            }
                            minsn.desc = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
                            minsn.name = "getOrDefault";
                            method.instructions.insertBefore(minsn, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;"));
                            method.instructions.insert(minsn, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J"));
                            method.instructions.insert(minsn, new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Long"));
                        } else {
                            throw new IllegalStateException("Unexpected edge case during transformation: Unknown method " + minsn.name + minsn.desc);
                        }

                        minsn.owner = TextraMASMTransformer.JU_MAP;
                        minsn.setOpcode(Opcodes.INVOKEINTERFACE);
                        minsn.itf = true;
                        transformed = true;
                    }
                    break;
                }
                case Opcodes.INVOKESPECIAL:
                    if (((MethodInsnNode) insn).owner.equals(TextraMASMTransformer.O2L_LONG_MAP) && ((MethodInsnNode) insn).name.equals("<init>")) {
                        ((MethodInsnNode) insn).owner = TextraMASMTransformer.JU_HASHMAP;
                        transformed = true;
                    }
                    break;
                case Opcodes.NEW:
                    if (((TypeInsnNode) insn).desc.equals(TextraMASMTransformer.O2L_LONG_MAP)) {
                        ((TypeInsnNode) insn).desc = TextraMASMTransformer.JU_HASHMAP;
                        transformed = true;
                    }
                    break;
                }
            }
        }

        return transformed;
    }
}
