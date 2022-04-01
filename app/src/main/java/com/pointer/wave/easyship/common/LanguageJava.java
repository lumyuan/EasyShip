/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided"as is". Use at your own risk.
 */
package com.pointer.wave.easyship.common;

/**
 * Singleton class containing the symbols and operators of the Java language
 */
public class LanguageJava extends Language {
	private static Language _theOne = null;

	private final static String[] keywords = {

	};

    private final static String[] function = {
			"r.TCQualityGrade", "foliage.MinLOD","foliage.LODDistanceScale","+CVars=","r.PUBGDeviceFPSLow","r.PUBGDeviceFPSMid","r.PUBGDeviceFPSHigh","r.PUBGDeviceFPSHDR","r.UserVulkanSetting","r.PUBGQualityLevel","r.PUBGMaxSupportQualityLevel","r.PUBGMSAASupport","r.PUBGLDR","r.PUBGCRLRuntmieMinMem","r.PUBGRenderSwitch","r.PUBGLimit","r.PUBGVersion","r.MobileContentScaleFactor","r.Mobile.TonemapperFilm","r.Mobile.AlwaysResolveDepth","r.MobileMSAA","r.Mobile.ForceDepthResolve","r.Mobile.TextureMipmapBias","r.MobileSimpleShader","r.MobileNumDynamicPointLights","r.Mobile.EarlyZPass","r.Mobile.SceneColorFormat","r.UserQualitySetting","r.UserHDRSetting","r.UseProgramBinaryCache","r.UseShaderPrecompileCount","r.UseShaderPrecompileMemLimit","r.UserMSAASetting","r.ShadowQuality","r.Shadow.MaxCSMResolution","r.Shadow.CSM.MaxMobileCascades","r.Shadow.DistanceScale","r.MetalVertexParameterSize","r.MetalPixelParameterSize","r.MetalComputeParameterSize","r.MaterialQualityLevel","r.BinningControlHintQCOMDriver","r.BinningControlHintQCOM","r.Android.DisableVulkanSupport","r.Android.DisableOpenGLES31Support","r.ACESStyle","r.BloomQuality","r.DefaultFeature.AntiAliasing","r.Decal.StencilSizeThreshold","r.DepthOfFieldQuality","r.DetailMode","r.EmitterSpawnRateScale","r.LightShaftQuality","r.MSAACount","r.MaxAnisotropy","r.NumBufferedOcclusionQueries","r.ParticleLODBias","r.RefractionQuality","r.StaticMeshLODDistanceScale","r.StaticMeshLODLevelLimited","r.Streaming.PoolSize","r.TextureStreaming","r.TagCullingSupport"
	};

    private final static char[] BASIC_C_OPERATORS = {
        '(', ')', '{', '}', '.', ',', ';', '=', '+', '-',
        '/', '*', '&', '!', '|', ':', '[', ']', '<', '>',
        '?', '~', '%', '^'
	};

	public static Language getInstance() {
		if (_theOne == null) {
			_theOne = new LanguageJava();
		}
		return _theOne;
	}

	private LanguageJava() {
		updateUserWord();
        setOperators(BASIC_C_OPERATORS);
		setKeywords(keywords);
        setNames(function);
	}

	/**
	 * Java has no preprocessors. Override base class implementation
	 */
	public boolean isLineAStart(char c) {
		return false;
	}
}
