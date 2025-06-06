# /project_root/database.py
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# Reemplaza con tus credenciales y detalles de la base de datos MySQL
# Formato: "mysql+mysqlconnector://usuario:contraseña@host:puerto/nombre_db"
SQLALCHEMY_DATABASE_URL = "mysql+mysqlconnector://mmarco:%40System345@192.168.35.25:3306/planif"

engine = create_engine(SQLALCHEMY_DATABASE_URL)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

def init_db():
    # Importa todos los modelos aquí para que Base los conozca antes de crear las tablas
    # from . import models # Descomentar cuando los modelos estén en models.py
    Base.metadata.create_all(bind=engine)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
