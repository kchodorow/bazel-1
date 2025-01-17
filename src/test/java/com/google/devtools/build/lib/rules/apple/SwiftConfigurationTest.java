// Copyright 2017 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.rules.apple;

import static com.google.common.truth.Truth.assertThat;

import com.google.devtools.build.lib.analysis.ConfiguredTarget;
import com.google.devtools.build.lib.analysis.SkylarkProviders;
import com.google.devtools.build.lib.analysis.util.BuildViewTestCase;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for Skylark interface to SwiftConfiguration. */
@RunWith(JUnit4.class)
public class SwiftConfigurationTest extends BuildViewTestCase {
  @Test
  public void testSkylarkApi() throws Exception {
    scratch.file("examples/rule/BUILD");
    scratch.file(
        "examples/rule/apple_rules.bzl",
        "def swift_binary_impl(ctx):",
        "   copts = ctx.fragments.swift.copts()",
        "   wmo = ctx.fragments.swift.enable_whole_module_optimization()",
        "   return struct(",
        "      copts=copts,",
        "      wmo=wmo,",
        "   )",
        "swift_binary = rule(",
        "   implementation = swift_binary_impl,",
        "   fragments = ['swift']",
        ")");

    scratch.file("examples/swift_skylark/a.m");
    scratch.file(
        "examples/swift_skylark/BUILD",
        "package(default_visibility = ['//visibility:public'])",
        "load('/examples/rule/apple_rules', 'swift_binary')",
        "swift_binary(",
        "   name='my_target',",
        ")");

    useConfiguration("--swiftcopt=foo", "--swiftcopt=bar", "--swift_whole_module_optimization");
    ConfiguredTarget skylarkTarget = getConfiguredTarget("//examples/swift_skylark:my_target");

    SkylarkProviders skylarkProviders = skylarkTarget.getProvider(SkylarkProviders.class);

    @SuppressWarnings("unchecked")
    List<String> copts = (List<String>) skylarkProviders.getValue("copts");

    assertThat(copts).containsAllOf("foo", "bar");
    assertThat(skylarkProviders.getValue("wmo", Boolean.class)).isTrue();
  }
}
