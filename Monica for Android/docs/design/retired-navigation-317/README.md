# 预览功能清理

[本地可编辑 Canvas](http://127.0.0.1:5186/#docz=nZVdT9swFIb_CvJ1LmjpJta7cYcmdrW7CU1u4lILY0eJW2CoEhsrLYyPso0xKmDQDYHQKEgTH6PApP2Wxkmu9hdmN21J-ShiqlTJJ-85ft6TY2cCcMwJAnEwwCjWYdefky6__M7bKTizX72pC3Gac4vTQANJC44omZliFMm1SSBPMmtEhiA1LIYNFYQEcY6eoXGlTFsmUVKeQip1AhjQGgbxJCQ20kCC8dQAM5DdiGQbe8j1ywkgy8XBCMRU5tNg5zCWwhSrFX-y5Byce4U9qRoD8W4NyI27s4MaGLJY2gyVSiFoBKJINFBpAI5hqZALDWCORq6pu2U4Ay0MKZeRJCYEqQrDmCoFZ-ZT0-yDlgwRmEDkGqCqqTMqo7aOEdURyA5mtWZ5LGuGYZ5EO9JI-T00aIyHQGoXH_29OaeyIf_F5E6LyMavZSMjsSYbTRMSwmImx4zabWSRWMdGNVI60xFs836ZFyIU1ZIzuytWjv1S0VtZFG9_iKOKmJvxP1QUZ9o0mcUxHZJSp7hUO30vqksywSmseFuhtJOqM7vZSms0PINtnMAE8_FXLJlU5UYx11Mgzq20nDs9hfRhidecw6ApPb29GrCggdP2C2aCeDTWXPYxzpkc89j1PkUearoP81FoGYh2OWfL_tSuO3ssJt-IzcUbltd2_a0jf70c6MSXS_f7mVOcE_vbQU5ousapfrfFYHm7w_sNRh9q0MkvOofqPbmfqjc81Z81bBXyYr7c0jWtmKhe_39e1w0z0VhosHXCbNQ21rHOY11P6DzUibTciIbN5376K_ugjfDWY0YZb4d51NMRRukfdPzDF-Pf81Lt97q7vOrl95xKqfZrRmzm3f3L2umCu3Ao1mbE50OnvOHktp3Fb1dXRM9d7DDThv440hkdZu7pYv11Pa9XtRFBOkdGPzVQcJlfIVyZU98TmAj2aLX-YNrdDB0KwvRhoJhbgvktZy53JRiFEoO3SeRV6R2ED1YKI2K0SbzKpXsRumhs-a2Tw22rq139_gE)

沿用应用原有动态配色、字体与普通导航，不新增交互。预览弹窗保留其余三项，滚动内容和底部关闭按钮适配大字体。

## 原生渲染验证

两版均运行真实 SimpleMainScreen 与 PreviewFeaturesDialog。常规尺寸和 360×640 dp、1.5 倍字体、手势导航下，FAB/最近使用入口不覆盖导航；弹窗内容可滚动，关闭按钮始终可达。大字体截图停在滚动底部，因此上方条目部分出界属于滚动状态。

| 版本 | 普通导航 | 预览弹窗 | 小屏大字体导航 | 小屏大字体弹窗 |
| --- | --- | --- | --- | --- |
| 普通版 | [截图](main-suite-ordinary-navigation.png) | [截图](main-suite-preview-features.png) | [截图](main-ui-ordinary-navigation.png) | [截图](main-ui-preview-features.png) |
| F-Droid | [截图](fdroid-suite-ordinary-navigation.png) | [截图](fdroid-suite-preview-features.png) | [截图](fdroid-ui-ordinary-navigation.png) | [截图](fdroid-ui-preview-features.png) |
