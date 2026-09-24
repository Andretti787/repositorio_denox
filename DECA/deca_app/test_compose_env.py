#!/usr/bin/env python3
"""Script de diagnóstico para verificar que el .env está completo antes de
desplegar en el servidor CentOS con Docker Compose (env_file)."""
import os
import pathlib
import sys

raiz = pathlib.Path(__file__).resolve().parent
env_file = raiz / ".env"

def main():
    print(f"Raiz del proyecto detectada: {raiz}")
    print(f" Directorio de trabajo actual: {os.getcwd()}")
    print(f" Fichero .env buscado en: {env_file}")
    print()

    if not env_file.is_file():
        print(f"❌ ERROR: .env NO existe en {env_file}")
        print(f"   El fichero .env DEBE estar en {raiz}, junto a docker-compose.yml")
        sys.exit(1)

    print(f"✅ Fichero .env encontrado: {env_file}")
    print(f"   Tamaño: {env_file.stat().st_size} bytes")
    print()

    # Leerlo como hace Compose
    variables = {}
    with env_file.open(encoding="utf-8") as f:
        for linea in f:
            linea = linea.strip()
            if not linea or linea.startswith("#"):
                continue
            if "=" not in linea:
                continue
            clave, valor = linea.split("=", 1)
            variables[clave.strip()] = valor.strip()

    print(f"📋 Variables leídas del .env: {len(variables)}")
    print()

    print("🔐 Variables clave (DB_ y DECA_CARGADOR):")
    for k, v in sorted(variables.items()):
        if k.startswith(("DB_", "DECA_CARGADOR")):
            if k in ("DB_PASSWORD",):
                v_mostrar = v[:4] + "..." + v[-4:] if len(v) > 8 else v
            elif k == "DECA_CARGADOR_DOMICILIO":
                v_mostrar = v[:30] + "..."
            else:
                v_mostrar = v
            print(f"    {k}: {v_mostrar}")
    print()

    print("🛡️ Variables obligatorias de SQL Server:")
    obligatorias = ["DB_SERVER", "DB_DATABASE", "DB_USERNAME", "DB_PASSWORD"]
    todas_ok = True
    for var in obligatorias:
        if var not in variables or not variables[var]:
            print(f"    ❌ [FALTA] {var}")
            todas_ok = False
        else:
            print(f"    ✅ [OK] {var} definido")

    print()
    if todas_ok:
        print("✅ RESULTADO: El .env está completo y listo para Docker Compose.")
        print()
        print("📝 Procedimiento en el servidor CentOS:")
        print(f"   cd /ruta/a/deca_app          # donde está docker-compose.yml")
        print("   docker compose config | grep DB_     # verifica que Compose lee las variables")
        print("   docker compose up -d --build --force-recreate")
        print("   docker compose logs -f deca-app | head -20")
        print("   curl -s http://localhost:5010/health")
    else:
        print("❌ RESULTADO: Faltan variables obligatorias. Complete el .env")
        sys.exit(1)

if __name__ == "__main__":
    main()
