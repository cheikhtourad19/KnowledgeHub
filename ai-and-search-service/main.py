from fastapi import FastAPI
from app.routers import ai_router, search_router

app = FastAPI(
    title="KnowledgeHub AI and Search Service",
    description="AI-powered search and recommendations",
    version="1.0.0"
)

# Include routers
app.include_router(ai_router.router, prefix="/ai", tags=["AI"])
app.include_router(search_router.router, prefix="/search", tags=["Search"])

@app.get("/")
async def root():
    return {
        "service": "AI and Search Service",
        "status": "running",
        "version": "1.0.0"
    }

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
