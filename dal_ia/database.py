#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Módulo de conexión a base de datos MySQL para Ficha de Paletización
"""

import os
import mysql.connector
from mysql.connector import Error
from dotenv import load_dotenv

# Cargar variables de entorno
load_dotenv()

class DatabaseConnection:
    """Clase para manejar la conexión a la base de datos MySQL"""
    
    def __init__(self):
        self.connection = None
        self.host = os.getenv('DB_HOST', '192.168.35.25')
        self.port = int(os.getenv('DB_PORT', 3306))
        self.user = os.getenv('DB_USER', 'mmarco')
        self.password = os.getenv('DB_PASSWORD', '@System345')
        self.database = os.getenv('DB_NAME', 'dalia')
    
    def create_connection(self):
        """Crear conexión a la base de datos"""
        try:
            self.connection = mysql.connector.connect(
                host=self.host,
                port=self.port,
                user=self.user,
                password=self.password,
                database=self.database,
                connection_timeout=10
            )
            if self.connection.is_connected():
                print(f"Conexión exitosa a MySQL: {self.host}:{self.port}/{self.database}")
                return True
        except Error as e:
            print(f"Error al conectar a MySQL: {e}")
            return False
    
    def close_connection(self):
        """Cerrar conexión a la base de datos"""
        if self.connection and self.connection.is_connected():
            self.connection.close()
            print("Conexión a MySQL cerrada")
    
    def execute_query(self, query, params=None):
        """Ejecutar consulta y retornar resultados"""
        if not self.connection or not self.connection.is_connected():
            if not self.create_connection():
                return None
        
        try:
            cursor = self.connection.cursor(dictionary=True)
            cursor.execute(query, params)
            results = cursor.fetchall()
            cursor.close()
            return results
        except Error as e:
            print(f"Error al ejecutar consulta: {e}")
            return None
    
    def execute_update(self, query, params=None):
        """Ejecutar consulta de actualización/inserción"""
        if not self.connection or not self.connection.is_connected():
            if not self.create_connection():
                return None
        
        try:
            cursor = self.connection.cursor()
            cursor.execute(query, params)
            self.connection.commit()
            lastrowid = cursor.lastrowid
            cursor.close()
            return lastrowid
        except Error as e:
            print(f"Error al ejecutar actualización: {e}")
            self.connection.rollback()
            return None
    
    def init_db(self):
        """Inicializar la base de datos con las tablas necesarias"""
        if not self.connection or not self.connection.is_connected():
            if not self.create_connection():
                return False
        
        try:
            cursor = self.connection.cursor()
            
            # Tabla principal de fichas de paletización
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS fichas_paletizacion (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    cliente VARCHAR(255),
                    numero_cliente VARCHAR(100),
                    destino VARCHAR(255),
                    empaquetado_individual TINYINT DEFAULT 0,
                    etiquetado_especial TINYINT DEFAULT 0,
                    etiquetado_especial_detalle TEXT,
                    separacion_referencias TINYINT DEFAULT 0,
                    separacion_referencias_detalle TEXT,
                    documentacion_adjunta TINYINT DEFAULT 0,
                    documentacion_adjunta_detalle TEXT,
                    altura_maxima VARCHAR(100),
                    peso_maximo VARCHAR(100),
                    tipo_palet VARCHAR(100),
                    palet_apilable TINYINT DEFAULT 0,
                    distribucion_mercancia TEXT,
                    lleva_carteles TINYINT DEFAULT 0,
                    lleva_carteles_detalle TEXT,
                    etiquetas TINYINT DEFAULT 0,
                    etiquetas_detalle1 TEXT,
                    etiquetas_detalle2 TEXT,
                    etiquetas_detalle3 TEXT,
                    codigo_sscc TINYINT DEFAULT 0,
                    codigo_sscc_detalle TEXT,
                    packing_list TINYINT DEFAULT 0,
                    packing_list_detalle TEXT,
                    etiqueta_transporte TINYINT DEFAULT 0,
                    etiqueta_transporte_detalle TEXT,
                    observaciones TEXT,
                    logo_data LONGTEXT,
                    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            ''')
            
            self.connection.commit()
            cursor.close()
            print(f"Base de datos inicializada en: {self.host}/{self.database}")
            return True
        except Error as e:
            print(f"Error al inicializar base de datos: {e}")
            return False

    def get_clientes(self):
        """Obtener lista de clientes desde dwdb.DIM_CTE"""
        query = 'SELECT CTE_COD, CTE_RAZON_SOCIAL FROM dwdb.DIM_CTE WHERE CTE_COD NOT LIKE "T%" ORDER BY CTE_COD'
        return self.execute_query(query)
    
    def get_cliente_por_codigo(self, cte_cod):
        """Obtener un cliente específico por su código"""
        query = 'SELECT CTE_COD, CTE_RAZON_SOCIAL FROM dwdb.DIM_CTE WHERE CTE_COD = %s'
        resultados = self.execute_query(query, (cte_cod,))
        return resultados[0] if resultados and len(resultados) > 0 else None

# Instancia global para reutilizar conexión
db = DatabaseConnection()

# Inicializar la base de datos
db.init_db()
