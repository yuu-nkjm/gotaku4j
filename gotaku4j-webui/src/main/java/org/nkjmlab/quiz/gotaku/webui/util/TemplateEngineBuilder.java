package org.nkjmlab.quiz.gotaku.webui.util;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

public class TemplateEngineBuilder {


  private String prefix = "";
  private String suffix = "";
  private long cacheTtlMs = 0;

  public TemplateEngineBuilder setPrefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  public TemplateEngineBuilder setSuffix(String suffix) {
    this.suffix = suffix;
    return this;
  }

  public TemplateEngineBuilder setTtlMs(long cacheTtlMs) {
    this.cacheTtlMs = cacheTtlMs;
    return this;
  }

  public TemplateEngine build() {
    TemplateEngine templateEngine = new org.thymeleaf.TemplateEngine();
    templateEngine.setTemplateResolver(createTemplateResolver(prefix, suffix, cacheTtlMs));
    return templateEngine;
  }


  private static ITemplateResolver createTemplateResolver(String prefix, String suffix,
      long cacheTtlMs) {
    final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setTemplateMode(TemplateMode.HTML);
    templateResolver.setPrefix(prefix);
    templateResolver.setSuffix(suffix);
    templateResolver.setCacheTTLMs(cacheTtlMs);
    return templateResolver;
  }



}
