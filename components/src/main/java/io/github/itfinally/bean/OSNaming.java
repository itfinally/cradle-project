package io.github.itfinally.bean;

public enum OSNaming {
  WINDOWS, LINUX, OSX, SOLARIS, UNKNOWN;

  private volatile static OSNaming naming;

  public static OSNaming detection() {
    if ( naming != null ) {
      return naming;
    }

    String osName = System.getProperty( "os.name" ).trim().toLowerCase();

    if ( osName.contains( "windows" ) ) {
      naming = WINDOWS;

    } else if ( osName.contains( "mac" ) ) {
      naming = OSX;

    } else if ( osName.contains( "nix" ) || osName.contains( "nux" ) || osName.contains( "aix" ) ) {
      naming = LINUX;

    } else if ( osName.contains( "sunos" ) ) {
      naming = SOLARIS;

    } else {
      naming = UNKNOWN;
    }

    return naming;
  }
}
