{
  description = "scala-workshop";

  inputs = {
    nixpkgs.url = github:nixos/nixpkgs/nixpkgs-unstable;
    flake-utils.url = github:numtide/flake-utils;
  };

  outputs = { self, nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [
            (f: p: {
              scala-cli = p.scala-cli.override { jre = p.temurin-bin-17; };
            })
          ];
        };
        jdk = pkgs.temurin-bin-17;


        jvmInputs = with pkgs; [
          jdk
          scalafmt
          scala-cli
        ];

        jvmHook = ''
          JAVA_HOME="${jdk}"
        '';

      in
      {
        devShells.default = pkgs.mkShell {
          name = "scala-workshop-dev-shell";
          buildInputs = jvmInputs;
          shellHook = jvmHook;
        };
      }
    );

}

